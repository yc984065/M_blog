package com.mrblue.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mrblue.entity.Keyword;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KeywordMapper extends BaseMapper<Keyword> {
    // 获取所有关键词
    @Select("SELECT * FROM keywords")
    List<Keyword> getAllKeywords();
}