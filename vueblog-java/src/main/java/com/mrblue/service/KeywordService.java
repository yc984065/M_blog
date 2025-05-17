package com.mrblue.service;

import com.baomidou.mybatisplus.extension.service.IService; // 引入 IService
import com.mrblue.entity.Keyword; // 引入 Keyword 实体
import java.util.List;

public interface KeywordService extends IService<Keyword> { // 继承 IService
    // 您已有的方法，用于内容检查
    String checkAndReplaceKeywords(String content);

    // 新增：添加关键字 (可以接收一个 Keyword 对象或仅关键字文本)
    boolean addKeyword(Keyword keyword); // 或者 boolean addKeyword(String keywordText);

    // 新增：删除关键字
    boolean deleteKeywordById(Long id);

    // 新增：获取所有关键字实体列表 (IService 已提供 list() 方法)
    // List<Keyword> getAllKeywords();

    // 新增：获取所有关键字字符串列表 (用于过滤)
    List<String> getAllKeywordStrings();
}