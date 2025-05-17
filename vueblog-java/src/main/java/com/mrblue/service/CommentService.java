package com.mrblue.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mrblue.common.dto.CommentDto;
import com.mrblue.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {
    List<CommentDto> getCommentsByBlogId(Long blogId);
    CommentDto addComment(Long blogId, String content, Long userId);
    boolean deleteComment(Long commentId, Long currentUserId, String currentUserRole);
    IPage<CommentDto> getAllCommentsAdmin(Page<CommentDto> page, String queryContent);
}