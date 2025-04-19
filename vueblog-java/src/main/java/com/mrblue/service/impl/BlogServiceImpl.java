package com.mrblue.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrblue.entity.Blog;
import com.mrblue.mapper.BlogMapper;
import com.mrblue.mapper.CategoryMapper;
import com.mrblue.mapper.TagMapper;
import com.mrblue.service.BlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private TagMapper tagMapper;

    @Override
    public List<Blog> searchBlogs(String query, Long categoryId, Long tagId) {
        return blogMapper.searchBlogs(query, categoryId, tagId);
    }

}
