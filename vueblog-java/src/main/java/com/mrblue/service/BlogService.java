package com.mrblue.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mrblue.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BlogService extends IService<Blog> {
    List<Blog> searchBlogs(String query, Long categoryId, Long tagId);
    IPage<Blog> getBlogsByPage(Integer currentPage, Integer pageSize);
    Blog getBlogWithRelations(Long id);
    void saveOrUpdateBlog(Blog blog);
    List<Blog> searchBlogsWithRelations(String query, Long categoryId, Long tagId);
}