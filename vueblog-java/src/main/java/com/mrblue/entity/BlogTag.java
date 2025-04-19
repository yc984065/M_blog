package com.mrblue.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("blog_tag")
public class BlogTag {
    private Long id;
    private Long blogId;
    private Long tagId;

}