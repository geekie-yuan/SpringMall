package site.geekie.shop.shoppingmall.service;

import site.geekie.shop.shoppingmall.dto.CartItemDTO;
import site.geekie.shop.shoppingmall.vo.CartItemVO;

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
     * @param userId 当前用户ID
     * @return 购物车项列表
     */
    List<CartItemVO> getCartItems(Long userId);

    /**
     * 添加商品到购物车
     * 如果商品已在购物车中，则增加数量
     *
     * @param request 购物车请求
     * @param userId 当前用户ID
     * @return 购物车项响应
     */
    CartItemVO addToCart(CartItemDTO request, Long userId);

    /**
     * 更新购物车项数量
     *
     * @param id 购物车项ID
     * @param quantity 新数量
     * @param userId 当前用户ID
     * @return 更新后的购物车项
     */
    CartItemVO updateQuantity(Long id, Integer quantity, Long userId);

    /**
     * 更新购物车项选中状态
     *
     * @param id 购物车项ID
     * @param checked 选中状态（0-未选中，1-已选中）
     * @param userId 当前用户ID
     */
    void updateChecked(Long id, Integer checked, Long userId);

    /**
     * 批量更新购物车选中状态（全选/取消全选）
     *
     * @param checked 选中状态（0-未选中，1-已选中）
     * @param userId 当前用户ID
     */
    void updateAllChecked(Integer checked, Long userId);

    /**
     * 删除购物车项
     *
     * @param id 购物车项ID
     * @param userId 当前用户ID
     */
    void deleteCartItem(Long id, Long userId);

    /**
     * 批量删除购物车项
     *
     * @param ids 购物车项ID列表
     * @param userId 当前用户ID
     */
    void deleteCartItems(List<Long> ids, Long userId);

    /**
     * 清空购物车
     *
     * @param userId 当前用户ID
     */
    void clearCart(Long userId);

    /**
     * 计算已选中商品的总价
     *
     * @param userId 当前用户ID
     * @return 总价
     */
    BigDecimal getCartTotal(Long userId);

    /**
     * 获取购物车商品种类数
     *
     * @param userId 当前用户ID
     * @return 商品种类数
     */
    int getCartCount(Long userId);
}
