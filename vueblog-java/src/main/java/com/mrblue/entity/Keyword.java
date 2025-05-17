package com.mrblue.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@TableName("keywords")
public class Keyword {
    @Setter
    @Getter
    private int id;
    private String keyword;

}