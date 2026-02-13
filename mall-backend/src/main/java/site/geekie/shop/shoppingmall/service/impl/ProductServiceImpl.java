package site.geekie.shop.shoppingmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.common.PageResult;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.dto.ProductDTO;
import site.geekie.shop.shoppingmall.entity.CategoryDO;
import site.geekie.shop.shoppingmall.entity.ProductDO;
import site.geekie.shop.shoppingmall.vo.ProductVO;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.mapper.CategoryMapper;
import site.geekie.shop.shoppingmall.mapper.ProductMapper;
import site.geekie.shop.shoppingmall.service.ProductService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 * 实现商品的CRUD操作和库存管理
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public PageResult<ProductVO> getAllProducts(int page, int size, String keyword, Long categoryId, Integer status) {
        PageHelper.startPage(page, size);
        List<ProductDO> products = productMapper.findAllWithFilter(keyword, categoryId, status);
        PageInfo<ProductDO> pageInfo = new PageInfo<>(products);
        List<ProductVO> list = convertListToResponses(products);
        return new PageResult<>(list, pageInfo.getTotal(), page, size);
    }

    @Override
    public List<ProductVO> getProductsByCategoryId(Long categoryId) {
        List<ProductDO> products = productMapper.findByCategoryId(categoryId);
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> getProductsByStatus(Integer status) {
        List<ProductDO> products = productMapper.findByStatus(status);
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ProductVO> searchProducts(String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<ProductDO> products = productMapper.searchByKeyword(keyword);
        PageInfo<ProductDO> pageInfo = new PageInfo<>(products);
        List<ProductVO> list = convertListToResponses(products);
        return new PageResult<>(list, pageInfo.getTotal(), page, size);
    }

    @Override
    public ProductVO getProductById(Long id) {
        ProductDO product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return convertToResponse(product);
    }

    @Override
    @Transactional
    public ProductVO addProduct(ProductDTO request) {
        // 1. 验证分类是否存在
        CategoryDO category = categoryMapper.findById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 2. 创建商品
        ProductDO product = new ProductDO();
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setSubtitle(request.getSubtitle());
        product.setMainImage(request.getMainImage());
        product.setImages(request.getImages());
        product.setDetail(request.getDetail());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());

        productMapper.insert(product);

        return convertToResponse(product);
    }

    @Override
    @Transactional
    public ProductVO updateProduct(Long id, ProductDTO request) {
        // 1. 查询商品是否存在
        ProductDO product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 如果要修改分类，验证分类是否存在
        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategoryId())) {
            CategoryDO category = categoryMapper.findById(request.getCategoryId());
            if (category == null) {
                throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
            }
        }

        // 3. 更新商品信息
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setSubtitle(request.getSubtitle());
        product.setMainImage(request.getMainImage());
        product.setImages(request.getImages());
        product.setDetail(request.getDetail());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());

        productMapper.updateById(product);

        return convertToResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        // 1. 查询商品是否存在
        ProductDO product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 删除商品
        productMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void decreaseStock(Long id, Integer quantity) {
        // 1. 查询商品是否存在
        ProductDO product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 扣减库存（使用乐观锁）
        int rows = productMapper.decreaseStock(id, quantity);
        if (rows == 0) {
            throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
        }
    }

    @Override
    @Transactional
    public void increaseStock(Long id, Integer quantity) {
        // 1. 查询商品是否存在
        ProductDO product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 增加库存
        productMapper.increaseStock(id, quantity);
    }

    /**
     * 批量将实体转换为响应DTO，对分类使用 IN 查询避免 N+1
     */
    private List<ProductVO> convertListToResponses(List<ProductDO> products) {
        List<Long> categoryIds = products.stream()
                .map(ProductDO::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, CategoryDO> categoryMap = categoryIds.isEmpty()
                ? Collections.emptyMap()
                : categoryMapper.findByIds(categoryIds).stream()
                        .collect(Collectors.toMap(CategoryDO::getId, c -> c));

        return products.stream()
                .map(product -> {
                    ProductVO response = new ProductVO(
                            product.getId(),
                            product.getCategoryId(),
                            product.getName(),
                            product.getSubtitle(),
                            product.getMainImage(),
                            product.getImages(),
                            product.getDetail(),
                            product.getPrice(),
                            product.getStock(),
                            product.getStatus(),
                            product.getCreatedAt()
                    );
                    CategoryDO category = categoryMap.get(product.getCategoryId());
                    if (category != null) {
                        response.setCategoryName(category.getName());
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 将实体转换为响应DTO
     *
     * @param product 商品实体
     * @return 商品响应DTO
     */
    private ProductVO convertToResponse(ProductDO product) {
        ProductVO response = new ProductVO(
                product.getId(),
                product.getCategoryId(),
                product.getName(),
                product.getSubtitle(),
                product.getMainImage(),
                product.getImages(),
                product.getDetail(),
                product.getPrice(),
                product.getStock(),
                product.getStatus(),
                product.getCreatedAt()
        );

        // 填充分类名称
        CategoryDO category = categoryMapper.findById(product.getCategoryId());
        if (category != null) {
            response.setCategoryName(category.getName());
        }

        return response;
    }

    // ===== 管理员方法实现 =====

    @Override
    @Transactional
    public ProductVO createProduct(ProductDTO request) {
        // 直接调用addProduct，保持一致性
        return addProduct(request);
    }

    @Override
    @Transactional
    public void updateProductStatus(Long id, Integer status) {
        // 1. 查询商品是否存在
        ProductDO product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 验证状态值是否合法
        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.INVALID_PARAMETER, "状态值必须为0或1");
        }

        // 3. 更新状态
        ProductDO updateProduct = new ProductDO();
        updateProduct.setId(id);
        updateProduct.setStatus(status);
        productMapper.updateById(updateProduct);
    }

    @Override
    @Transactional
    public void updateProductStock(Long id, Integer stock) {
        // 1. 查询商品是否存在
        ProductDO product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 验证库存值是否合法
        if (stock < 0) {
            throw new BusinessException(ResultCode.INVALID_PARAMETER, "库存不能为负数");
        }

        // 3. 更新库存
        ProductDO updateProduct = new ProductDO();
        updateProduct.setId(id);
        updateProduct.setStock(stock);
        productMapper.updateById(updateProduct);
    }
}
