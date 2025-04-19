package com.mrblue.controller;

import com.mrblue.common.lang.Result;
import com.mrblue.service.OllamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {
    private final OllamaService ollamaService;

    @Autowired
    public AIController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @PostMapping("/ask")
    public Result ask(@RequestBody Map<String, String> params) {
        String prompt = params.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            return Result.fail("提示词不能为空");
        }
        return ollamaService.getModelResponse(prompt);
    }
}