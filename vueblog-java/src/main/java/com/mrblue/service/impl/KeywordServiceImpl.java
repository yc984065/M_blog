package com.mrblue.service.impl;

import com.mrblue.entity.Keyword;
import com.mrblue.mapper.KeywordMapper;
import com.mrblue.service.KeywordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class KeywordServiceImpl implements KeywordService {

    @Autowired
    private KeywordMapper keywordMapper;

    // 实现checkAndReplaceKeywords方法
    @Override
    public String checkAndReplaceKeywords(String content) {
        // 获取所有关键词
        List<Keyword> keywords = keywordMapper.getAllKeywords();

        // 遍历所有关键词，进行替换
        for (Keyword keyword : keywords) {
            String sensitiveWord = keyword.getKeyword();
            // 使用正则表达式进行不区分大小写的替换
            content = content.replaceAll("(?i)" + sensitiveWord, "**");
        }
        return content;
    }
}