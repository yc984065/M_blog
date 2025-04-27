package com.mrblue.service.impl;

import com.mrblue.entity.Blog;
import com.mrblue.mapper.BlogMapper;
import com.mrblue.service.BlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Autowired
    private BlogMapper blogMapper;

    @Override
    public List<Blog> searchBlogs(String query, Long categoryId, Long tagId) {
        return blogMapper.searchBlogs(query, categoryId, tagId);
    }

}
