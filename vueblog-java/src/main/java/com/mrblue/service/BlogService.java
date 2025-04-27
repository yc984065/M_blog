package com.mrblue.service;

import com.mrblue.common.lang.Result;
import com.mrblue.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface BlogService extends IService<Blog> {
    List<Blog> searchBlogs(String query, Long categoryId, Long tagId);
}
