package site.geekie.shop.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.dto.request.ProductRequest;
import site.geekie.shop.shoppingmall.dto.response.ProductResponse;
import site.geekie.shop.shoppingmall.entity.Category;
import site.geekie.shop.shoppingmall.entity.Product;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.mapper.CategoryMapper;
import site.geekie.shop.shoppingmall.mapper.ProductMapper;
import site.geekie.shop.shoppingmall.service.ProductService;

import java.util.List;
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
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productMapper.findAll();
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByCategoryId(Long categoryId) {
        List<Product> products = productMapper.findByCategoryId(categoryId);
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByStatus(Integer status) {
        List<Product> products = productMapper.findByStatus(status);
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        List<Product> products = productMapper.searchByKeyword(keyword);
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return convertToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse addProduct(ProductRequest request) {
        // 1. 验证分类是否存在
        Category category = categoryMapper.findById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 2. 创建商品
        Product product = new Product();
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
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        // 1. 查询商品是否存在
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 如果要修改分类，验证分类是否存在
        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategoryId())) {
            Category category = categoryMapper.findById(request.getCategoryId());
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
        Product product = productMapper.findById(id);
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
        Product product = productMapper.findById(id);
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
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 增加库存
        productMapper.increaseStock(id, quantity);
    }

    /**
     * 将实体转换为响应DTO
     *
     * @param product 商品实体
     * @return 商品响应DTO
     */
    private ProductResponse convertToResponse(Product product) {
        ProductResponse response = new ProductResponse(
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
        Category category = categoryMapper.findById(product.getCategoryId());
        if (category != null) {
            response.setCategoryName(category.getName());
        }

        return response;
    }

    // ===== 管理员方法实现 =====

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // 直接调用addProduct，保持一致性
        return addProduct(request);
    }

    @Override
    @Transactional
    public void updateProductStatus(Long id, Integer status) {
        // 1. 查询商品是否存在
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 验证状态值是否合法
        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.INVALID_PARAMETER, "状态值必须为0或1");
        }

        // 3. 更新状态
        Product updateProduct = new Product();
        updateProduct.setId(id);
        updateProduct.setStatus(status);
        productMapper.updateById(updateProduct);
    }

    @Override
    @Transactional
    public void updateProductStock(Long id, Integer stock) {
        // 1. 查询商品是否存在
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 验证库存值是否合法
        if (stock < 0) {
            throw new BusinessException(ResultCode.INVALID_PARAMETER, "库存不能为负数");
        }

        // 3. 更新库存
        Product updateProduct = new Product();
        updateProduct.setId(id);
        updateProduct.setStock(stock);
        productMapper.updateById(updateProduct);
    }
}
