package site.geekie.shop.shoppingmall.service;

import site.geekie.shop.shoppingmall.dto.request.CartItemRequest;
import site.geekie.shop.shoppingmall.dto.response.CartItemResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车服务接口
 * 提供购物车的业务逻辑方法
 */
public interface CartService {

    /**
     * 获取当前用户的购物车列表
     *
     * @return 购物车项列表
     */
    List<CartItemResponse> getCartItems();

    /**
     * 添加商品到购物车
     * 如果商品已在购物车中，则增加数量
     *
     * @param request 购物车请求
     * @return 购物车项响应
     */
    CartItemResponse addToCart(CartItemRequest request);

    /**
     * 更新购物车项数量
     *
     * @param id 购物车项ID
     * @param quantity 新数量
     * @return 更新后的购物车项
     */
    CartItemResponse updateQuantity(Long id, Integer quantity);

    /**
     * 更新购物车项选中状态
     *
     * @param id 购物车项ID
     * @param checked 选中状态（0-未选中，1-已选中）
     */
    void updateChecked(Long id, Integer checked);

    /**
     * 批量更新购物车选中状态（全选/取消全选）
     *
     * @param checked 选中状态（0-未选中，1-已选中）
     */
    void updateAllChecked(Integer checked);

    /**
     * 删除购物车项
     *
     * @param id 购物车项ID
     */
    void deleteCartItem(Long id);

    /**
     * 批量删除购物车项
     *
     * @param ids 购物车项ID列表
     */
    void deleteCartItems(List<Long> ids);

    /**
     * 清空购物车
     */
    void clearCart();

    /**
     * 计算已选中商品的总价
     *
     * @return 总价
     */
    BigDecimal getCartTotal();

    /**
     * 获取购物车商品种类数
     *
     * @return 商品种类数
     */
    int getCartCount();
}
