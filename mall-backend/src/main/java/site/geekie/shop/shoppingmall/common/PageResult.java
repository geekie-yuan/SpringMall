package site.geekie.shop.shoppingmall.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装类
 * 用于封装分页查询的结果数据
 *
 * @param <T> 列表数据的泛型类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    // 数据列表
    private List<T> list;

    // 总记录数
    private long total;

    // 当前页码
    private int page;

    // 每页大小
    private int size;

    // 总页数
    private int pages;

    /**
     * 构造分页结果（自动计算总页数）
     *
     * @param list 数据列表
     * @param total 总记录数
     * @param page 当前页码
     * @param size 每页大小
     */
    public PageResult(List<T> list, long total, int page, int size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = (int) Math.ceil((double) total / size);
    }
}
