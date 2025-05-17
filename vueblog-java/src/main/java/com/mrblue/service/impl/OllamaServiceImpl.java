package com.mrblue.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrblue.common.dto.OllamaRequest;
import com.mrblue.common.lang.Result;
import com.mrblue.service.OllamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OllamaServiceImpl implements OllamaService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    @Override
    public Result getModelResponse(String prompt) {
        try {
            // 1. 构造请求体
            OllamaRequest request = new OllamaRequest();
            request.setModel("gemma3:latest");
            request.setPrompt(
                    "\n" +
                            "原始内容：" + prompt
            );
            request.setOptions(Map.of(
                    "temperature", 0.6,  // 0.0-0.3	保守输出，选择最高概率词	事实回答/代码生成
                                                 // 0.4-0.7	平衡创造性和准确性	内容优化/写作建议
                                                 // 0.8-1.0+	高度创造性，可能不连贯	头脑风暴/诗歌创作
                    "num_predict", 500,      // 限制输出长度
                    "repeat_penalty", 1.2        // 避免重复,数值越高越减少重复次数 取值范围：1.0 - 2.0
            ));

            // 2. 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 3. 发送请求
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(
                    OLLAMA_URL,
                    new HttpEntity<>(request, headers),
                    String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            String aiResponse = root.path("response").asText();

            return Result.succ(aiResponse);
        } catch (Exception e) {
            return Result.fail("AI服务调用失败: " + e.getMessage());
        }
    }
}
