package site.geekie.shop.shoppingmall.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.geekie.shop.shoppingmall.common.ResultCode;
import site.geekie.shop.shoppingmall.dto.request.CategoryRequest;
import site.geekie.shop.shoppingmall.dto.response.CategoryResponse;
import site.geekie.shop.shoppingmall.entity.Category;
import site.geekie.shop.shoppingmall.exception.BusinessException;
import site.geekie.shop.shoppingmall.mapper.CategoryMapper;
import site.geekie.shop.shoppingmall.service.CategoryService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 * 实现商品分类的CRUD操作和树形结构构建
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryMapper.findAll();
        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getCategoryTree() {
        // 获取所有分类
        List<Category> allCategories = categoryMapper.findAll();

        // 转换为Response对象
        List<CategoryResponse> allResponses = allCategories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // 构建树形结构：只返回一级分类，children包含子分类
        return buildTree(allResponses, 0L);
    }

    @Override
    public List<CategoryResponse> getCategoriesByParentId(Long parentId) {
        List<Category> categories = categoryMapper.findByParentId(parentId);
        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }
        return convertToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse addCategory(CategoryRequest request) {
        // 1. 验证父分类存在性（如果不是顶级分类）
        if (request.getParentId() > 0) {
            Category parentCategory = categoryMapper.findById(request.getParentId());
            if (parentCategory == null) {
                throw new BusinessException(ResultCode.INVALID_PARENT_CATEGORY);
            }

            // 验证层级关系：父分类level + 1 应该等于当前分类level
            if (parentCategory.getLevel() + 1 != request.getLevel()) {
                throw new BusinessException(ResultCode.INVALID_PARENT_CATEGORY);
            }

            // 验证最大层级：不超过3级
            if (request.getLevel() > 3) {
                throw new BusinessException(ResultCode.INVALID_PARENT_CATEGORY);
            }
        } else {
            // 顶级分类level必须为1
            if (request.getLevel() != 1) {
                throw new BusinessException(ResultCode.INVALID_PARENT_CATEGORY);
            }
        }

        // 2. 检查同级分类名称是否重复
        Category existingCategory = categoryMapper.findByNameAndParentId(
                request.getName(), request.getParentId());
        if (existingCategory != null) {
            throw new BusinessException(ResultCode.CATEGORY_NAME_DUPLICATE);
        }

        // 3. 创建分类
        Category category = new Category();
        category.setName(request.getName());
        category.setParentId(request.getParentId());
        category.setLevel(request.getLevel());
        category.setSortOrder(request.getSortOrder());
        category.setIcon(request.getIcon());
        category.setStatus(request.getStatus());

        categoryMapper.insert(category);

        return convertToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        // 1. 查询分类是否存在
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 2. 检查名称是否与同级其他分类重复
        Category existingCategory = categoryMapper.findByNameAndParentId(
                request.getName(), category.getParentId());
        if (existingCategory != null && !existingCategory.getId().equals(id)) {
            throw new BusinessException(ResultCode.CATEGORY_NAME_DUPLICATE);
        }

        // 3. 更新分类信息（不允许修改parentId和level）
        category.setName(request.getName());
        category.setSortOrder(request.getSortOrder());
        category.setIcon(request.getIcon());
        category.setStatus(request.getStatus());

        categoryMapper.updateById(category);

        return convertToResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        // 1. 查询分类是否存在
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 2. 检查是否有子分类
        int childrenCount = categoryMapper.countByParentId(id);
        if (childrenCount > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_CHILDREN);
        }

        // 3. 检查是否有商品（待Product模块实现后补充）
        // TODO: 检查该分类下是否有商品

        // 4. 删除分类
        categoryMapper.deleteById(id);
    }

    /**
     * 构建分类树形结构
     *
     * @param allCategories 所有分类列表
     * @param parentId 父分类ID
     * @return 树形结构的分类列表
     */
    private List<CategoryResponse> buildTree(List<CategoryResponse> allCategories, Long parentId) {
        List<CategoryResponse> tree = new ArrayList<>();

        for (CategoryResponse category : allCategories) {
            if (category.getParentId().equals(parentId)) {
                // 递归查找子分类
                List<CategoryResponse> children = buildTree(allCategories, category.getId());
                if (!children.isEmpty()) {
                    category.setChildren(children);
                }
                tree.add(category);
            }
        }

        return tree;
    }

    /**
     * 将实体转换为响应DTO
     *
     * @param category 分类实体
     * @return 分类响应DTO
     */
    private CategoryResponse convertToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getParentId(),
                category.getLevel(),
                category.getSortOrder(),
                category.getIcon(),
                category.getStatus(),
                category.getCreatedAt()
        );
    }
}
