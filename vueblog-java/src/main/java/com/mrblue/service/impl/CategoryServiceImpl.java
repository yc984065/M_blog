package com.mrblue.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrblue.entity.BlogCategory;
import com.mrblue.entity.Category;
import com.mrblue.mapper.BlogCategoryMapper;
import com.mrblue.mapper.CategoryMapper;
import com.mrblue.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private BlogCategoryMapper blogCategoryMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> findByBlogId(Long blogId) {
        return baseMapper.findByBlogId(blogId);
    }

    @Override
    @Transactional
    public void updateBlogCategories(Long blogId, List<Long> categoryId) {
        categoryMapper.deleteBlogCategories(blogId);
        if (categoryId != null && !categoryId.isEmpty()) {
            categoryMapper.insertBlogCategories(blogId, categoryId);
        }
    }

}
