package site.geekie.shop.shoppingmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.common.PageResult;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.converter.ProductConverter;
import site.geekie.shop.shoppingmall.dto.ProductDTO;
import site.geekie.shop.shoppingmall.entity.CategoryDO;
import site.geekie.shop.shoppingmall.entity.ProductDO;
import site.geekie.shop.shoppingmall.vo.ProductVO;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.mapper.CategoryMapper;
import site.geekie.shop.shoppingmall.mapper.ProductMapper;
import site.geekie.shop.shoppingmall.service.ProductService;

import java.util.List;

/**
 * 商品服务实现类
 * 实现商品的CRUD操作和库存管理
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final ProductConverter productConverter;

    @Override
    public PageResult<ProductVO> getAllProducts(int page, int size, String keyword, Long categoryId, Integer status) {
        PageHelper.startPage(page, size);
        List<ProductDO> products = productMapper.findAllWithFilter(keyword, categoryId, status);
        PageInfo<ProductDO> pageInfo = new PageInfo<>(products);
        List<ProductVO> list = productConverter.toVOList(products, categoryMapper);
        return new PageResult<>(list, pageInfo.getTotal(), page, size);
    }

    @Override
    public List<ProductVO> getProductsByCategoryId(Long categoryId) {
        List<ProductDO> products = productMapper.findByCategoryId(categoryId);
        return productConverter.toVOList(products, categoryMapper);
    }

    @Override
    public List<ProductVO> getProductsByStatus(Integer status) {
        List<ProductDO> products = productMapper.findByStatus(status);
        return productConverter.toVOList(products, categoryMapper);
    }

    @Override
    public PageResult<ProductVO> searchProducts(String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<ProductDO> products = productMapper.searchByKeyword(keyword);
        PageInfo<ProductDO> pageInfo = new PageInfo<>(products);
        List<ProductVO> list = productConverter.toVOList(products, categoryMapper);
        return new PageResult<>(list, pageInfo.getTotal(), page, size);
    }

    @Override
    public ProductVO getProductById(Long id) {
        ProductDO product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return productConverter.toVOWithCategory(product, categoryMapper);
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

        return productConverter.toVOWithCategory(product, categoryMapper);
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

        return productConverter.toVOWithCategory(product, categoryMapper);
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
