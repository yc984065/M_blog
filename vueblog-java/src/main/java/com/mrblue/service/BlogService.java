package com.mrblue.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mrblue.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BlogService extends IService<Blog> {
    List<Blog> searchBlogs(String query, Long categoryId, Long tagId);

    // 修改 getBlogsByPage 方法签名以接收可选的 query 参数 (原为 title)
    IPage<Blog> getBlogsByPage(Integer currentPage, Integer pageSize, String query);

    Blog getBlogWithRelations(Long id);
    void saveOrUpdateBlog(Blog blog);
    List<Blog> searchBlogsWithRelations(String query, Long categoryId, Long tagId);
}