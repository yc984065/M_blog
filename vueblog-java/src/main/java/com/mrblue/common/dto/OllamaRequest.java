package com.mrblue.common.dto;

import lombok.Data;
import java.util.Map;

@Data
public class OllamaRequest {
    private String model;
    private String prompt;
    private boolean stream = false;
    private Map<String, Object> options;

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
}