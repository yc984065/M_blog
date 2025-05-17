package com.mrblue.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; // 用于查询
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl; // 引入 ServiceImpl
import com.mrblue.entity.Keyword;
import com.mrblue.mapper.KeywordMapper;
import com.mrblue.service.KeywordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 用于事务

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KeywordServiceImpl extends ServiceImpl<KeywordMapper, Keyword> implements KeywordService {

    @Override
    public String checkAndReplaceKeywords(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        List<String> keywords = getAllKeywordStrings();
        String tempContent = content;
        for (String keyword : keywords) {
            // 使用正则表达式进行不区分大小写的替换
            tempContent = tempContent.replaceAll("(?i)" + java.util.regex.Pattern.quote(keyword), "**");
        }
        return tempContent;
    }

    @Override
    @Transactional
    public boolean addKeyword(Keyword keyword) {
        if (keyword == null || keyword.getKeyword() == null || keyword.getKeyword().trim().isEmpty()) {
            throw new IllegalArgumentException("关键字不能为空");
        }
        // 检查关键字是否已存在
        Keyword existing = getOne(new QueryWrapper<Keyword>().eq("keyword", keyword.getKeyword().trim()));
        if (existing != null) {
            throw new IllegalArgumentException("关键字 '" + keyword.getKeyword() + "' 已存在");
        }
        return save(keyword);
    }

    @Override
    @Transactional
    public boolean deleteKeywordById(Long id) {
        return removeById(id);
    }

    @Override
    public List<String> getAllKeywordStrings() {
        List<Keyword> keywordEntities = list(); // 使用 ServiceImpl 的 list 方法获取所有 Keyword 实体
        if (keywordEntities == null) {
            return List.of(); // 返回空列表避免 NullPointerException
        }
        return keywordEntities.stream()
                .map(Keyword::getKeyword)
                .collect(Collectors.toList());
    }
}