package com.mrblue.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrblue.common.dto.CommentDto;
import com.mrblue.entity.Comment;
import com.mrblue.entity.User;
import com.mrblue.mapper.CommentMapper;
import com.mrblue.mapper.UserMapper;
import com.mrblue.service.CommentService;
import com.mrblue.service.KeywordService;
import com.mrblue.shiro.AccountProfile; // 用于获取当前用户信息
import com.mrblue.util.ShiroUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private KeywordService keywordService;

    @Override
    public List<CommentDto> getCommentsByBlogId(Long blogId) {
        List<CommentDto> comments = commentMapper.findCommentsByBlogIdWithUserDetails(blogId);
        return comments;
    }

    @Override
    @Transactional
    public CommentDto addComment(Long blogId, String content, Long userId) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }

        String filteredContent = keywordService.checkAndReplaceKeywords(content);

        Comment comment = new Comment();
        comment.setBlogId(blogId);
        comment.setUserId(userId);
        comment.setContent(filteredContent);
        comment.setCreatedAt(LocalDateTime.now());

        boolean saved = this.save(comment);
        if (!saved || comment.getId() == null) {
            throw new RuntimeException("发表评论失败");
        }

        CommentDto commentDto = new CommentDto();
        BeanUtils.copyProperties(comment, commentDto);
        commentDto.setContent(filteredContent);

        User user = userMapper.selectById(userId);
        if (user != null) {
            commentDto.setUsername(user.getUsername());
            commentDto.setUserAvatar(user.getAvatar());
            commentDto.setUserRole(user.getRole());
        } else {
            commentDto.setUsername("未知用户");
        }
        return commentDto;
    }

    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long currentUserId, String currentUserRole) {
        Comment comment = this.getById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在或已被删除");
        }

        boolean isAdmin = "admin".equals(currentUserRole);
        boolean isOwner = comment.getUserId().equals(currentUserId);

        if (isAdmin || isOwner) {
            log.info("用户(ID:{}, Role:{}) 正在删除评论ID:{}", currentUserId, currentUserRole, commentId);
            return this.removeById(commentId);
        } else {
            throw new UnauthorizedException("您没有权限删除此评论");
        }
    }

    @Override
    public IPage<CommentDto> getAllCommentsAdmin(Page<CommentDto> page, String queryContent) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(queryContent)) {
            queryWrapper.like("content", queryContent);
        }

        queryWrapper.orderByDesc("c.created_at");

        return commentMapper.findAllCommentsAdmin(page, queryWrapper);
    }
}