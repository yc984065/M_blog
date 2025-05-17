package com.mrblue.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrblue.entity.Keyword;
import java.util.List;

public interface KeywordService extends IService<Keyword> {

    String checkAndReplaceKeywords(String content);

    // 添加关键字
    boolean addKeyword(Keyword keyword);

    // 删除关键字
    boolean deleteKeywordById(Long id);

    // 获取所有关键字字符串列表
    List<String> getAllKeywordStrings();
}