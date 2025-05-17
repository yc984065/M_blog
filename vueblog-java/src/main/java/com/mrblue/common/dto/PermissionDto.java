package com.mrblue.common.dto;

import lombok.Data;

@Data
public class PermissionDto {
    private Long userId;
    private String role;
}
