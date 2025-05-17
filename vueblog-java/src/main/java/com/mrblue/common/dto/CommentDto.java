package com.mrblue.common.dto;

import lombok.Data;
import java.time.LocalDateTime;


@Data
public class CommentDto {
    private Long id;
    private Long blogId;
    private Long userId;
    private String username;
    private String userAvatar;
    private String userRole;
    private String content;
    private LocalDateTime createdAt;

}