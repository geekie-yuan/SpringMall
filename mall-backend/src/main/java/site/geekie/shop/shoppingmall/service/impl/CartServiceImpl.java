package site.geekie.shop.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.dto.request.CartItemRequest;
import site.geekie.shop.shoppingmall.dto.response.CartItemResponse;
import site.geekie.shop.shoppingmall.entity.CartItem;
import site.geekie.shop.shoppingmall.entity.Product;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.mapper.CartItemMapper;
import site.geekie.shop.shoppingmall.mapper.ProductMapper;
import site.geekie.shop.shoppingmall.security.SecurityUser;
import site.geekie.shop.shoppingmall.service.CartService;
import site.geekie.shop.shoppingmall.common.ResultCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return securityUser.getUser().getId();
    }

    /**
     * 填充商品详情到购物车响应对象
     */
    private CartItemResponse buildCartItemResponse(CartItem cartItem) {
        CartItemResponse response = new CartItemResponse();
        response.setId(cartItem.getId());
        response.setUserId(cartItem.getUserId());
        response.setProductId(cartItem.getProductId());
        response.setQuantity(cartItem.getQuantity());
        response.setChecked(cartItem.getChecked());
        response.setCreatedAt(cartItem.getCreatedAt());

        // 查询商品详情并填充
        Product product = productMapper.findById(cartItem.getProductId());
        if (product != null) {
            response.setProductName(product.getName());
            response.setProductSubtitle(product.getSubtitle());
            response.setProductImage(product.getMainImage());
            response.setProductPrice(product.getPrice());
            response.setProductStock(product.getStock());

            // 计算小计
            BigDecimal subtotal = product.getPrice().multiply(new BigDecimal(cartItem.getQuantity()));
            response.setSubtotal(subtotal);
        }

        return response;
    }

    /**
     * 验证购物车项所有权
     */
    private void validateCartItemOwnership(Long cartItemId) {
        CartItem cartItem = cartItemMapper.findById(cartItemId);
        if (cartItem == null) {
            throw new BusinessException(ResultCode.CART_ITEM_NOT_FOUND);
        }
        if (!cartItem.getUserId().equals(getCurrentUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    @Override
    public List<CartItemResponse> getCartItems() {
        Long userId = getCurrentUserId();
        List<CartItem> cartItems = cartItemMapper.findByUserId(userId);

        return cartItems.stream()
                .map(this::buildCartItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItemResponse addToCart(CartItemRequest request) {
        Long userId = getCurrentUserId();

        // 验证商品是否存在且可售
        Product product = productMapper.findById(request.getProductId());
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (product.getStatus() != 1) {
            throw new BusinessException(ResultCode.PRODUCT_UNAVAILABLE);
        }

        // 验证库存是否充足
        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
        }

        // 检查商品是否已在购物车中
        CartItem existingItem = cartItemMapper.findByUserIdAndProductId(userId, request.getProductId());

        if (existingItem != null) {
            // 商品已存在，增加数量
            int newQuantity = existingItem.getQuantity() + request.getQuantity();

            // 再次验证库存
            if (product.getStock() < newQuantity) {
                throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
            }

            cartItemMapper.updateQuantity(existingItem.getId(), newQuantity);
            existingItem.setQuantity(newQuantity);
            return buildCartItemResponse(existingItem);
        } else {
            // 新增购物车项
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setChecked(1); // 默认选中

            cartItemMapper.insert(cartItem);
            return buildCartItemResponse(cartItem);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItemResponse updateQuantity(Long id, Integer quantity) {
        // 验证所有权
        validateCartItemOwnership(id);

        if (quantity < 1) {
            throw new BusinessException(ResultCode.INVALID_PARAMETER);
        }

        // 获取购物车项
        CartItem cartItem = cartItemMapper.findById(id);

        // 验证商品库存
        Product product = productMapper.findById(cartItem.getProductId());
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (product.getStock() < quantity) {
            throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
        }

        // 更新数量
        cartItemMapper.updateQuantity(id, quantity);
        cartItem.setQuantity(quantity);

        return buildCartItemResponse(cartItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChecked(Long id, Integer checked) {
        // 验证所有权
        validateCartItemOwnership(id);

        if (checked != 0 && checked != 1) {
            throw new BusinessException(ResultCode.INVALID_PARAMETER);
        }

        cartItemMapper.updateChecked(id, checked);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAllChecked(Integer checked) {
        if (checked != 0 && checked != 1) {
            throw new BusinessException(ResultCode.INVALID_PARAMETER);
        }

        Long userId = getCurrentUserId();
        cartItemMapper.updateAllChecked(userId, checked);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCartItem(Long id) {
        // 验证所有权
        validateCartItemOwnership(id);

        cartItemMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCartItems(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.INVALID_PARAMETER);
        }

        // 验证所有购物车项的所有权
        Long userId = getCurrentUserId();
        for (Long id : ids) {
            CartItem cartItem = cartItemMapper.findById(id);
            if (cartItem == null || !cartItem.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }

        cartItemMapper.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart() {
        Long userId = getCurrentUserId();
        cartItemMapper.deleteByUserId(userId);
    }

    @Override
    public BigDecimal getCartTotal() {
        Long userId = getCurrentUserId();
        List<CartItem> checkedItems = cartItemMapper.findCheckedByUserId(userId);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : checkedItems) {
            Product product = productMapper.findById(item.getProductId());
            if (product != null) {
                BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(item.getQuantity()));
                total = total.add(itemTotal);
            }
        }

        return total;
    }

    @Override
    public int getCartCount() {
        Long userId = getCurrentUserId();
        return cartItemMapper.countByUserId(userId);
    }
}
