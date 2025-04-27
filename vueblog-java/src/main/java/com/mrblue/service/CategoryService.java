package com.mrblue.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrblue.entity.Category;
import java.util.List;

public interface CategoryService extends IService<Category> {
    List<Category> findByBlogId(Long blogId);
    void updateBlogCategories(Long blogId, List<Long> categoryIds);

}