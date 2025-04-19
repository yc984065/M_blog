package com.mrblue.service;

import com.mrblue.common.lang.Result;

public interface OllamaService {
    Result getModelResponse(String prompt);
}