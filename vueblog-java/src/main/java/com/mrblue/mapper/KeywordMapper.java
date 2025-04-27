package com.mrblue.mapper;

import com.mrblue.entity.Keyword;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface KeywordMapper {
    // 获取所有关键词
    @Select("SELECT * FROM keywords")
    List<Keyword> getAllKeywords();
}