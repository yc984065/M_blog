package com.mrblue.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("blog_category")
public class BlogCategory {
    private Long id;
    private Long blogId;
    private Long categoryId;
}