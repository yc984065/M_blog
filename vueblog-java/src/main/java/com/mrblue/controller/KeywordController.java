package com.mrblue.controller;

import com.mrblue.common.lang.Result;
import com.mrblue.entity.Keyword;
import com.mrblue.service.KeywordService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/keywords")
@RequiresAuthentication // 所有关键字操作都需要登录
public class KeywordController {

    @Autowired
    private KeywordService keywordService;

    @PostMapping("/add")
    @RequiresRoles("admin")
    public Result addKeyword(@RequestBody Keyword keyword) {
        try {
            keywordService.addKeyword(keyword);
            return Result.succ("关键字添加成功");
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @RequiresRoles("admin")
    public Result deleteKeyword(@PathVariable("id") Long id) {
        if (keywordService.deleteKeywordById(id)) {
            return Result.succ("关键字删除成功");
        }
        return Result.fail("关键字删除失败或不存在");
    }

    @GetMapping("/list")
    @RequiresRoles("admin") // 列表仅管理员可见
    public Result listKeywords() {
        List<Keyword> keywords = keywordService.list();
        return Result.succ(keywords);
    }
}