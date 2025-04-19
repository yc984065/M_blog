package com.mrblue.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("category")
public class Category {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime created;
}