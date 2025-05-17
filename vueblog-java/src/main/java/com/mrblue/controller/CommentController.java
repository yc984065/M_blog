package com.mrblue.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mrblue.common.dto.CommentDto;
import com.mrblue.common.lang.Result;
import com.mrblue.service.BlogService;
import com.mrblue.service.CommentService;
import com.mrblue.shiro.AccountProfile;
import com.mrblue.util.ShiroUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private BlogService blogService;

    @GetMapping("/blogs/{blogId}/comments")
    public Result getComments(@PathVariable("blogId") Long blogId) {
        if (blogService.getById(blogId) == null) {
            return Result.fail(404, "博客不存在", null);
        }
        List<CommentDto> comments = commentService.getCommentsByBlogId(blogId);
        return Result.succ(comments);
    }

    @PostMapping("/blogs/{blogId}/comments")
    @RequiresAuthentication
    public Result addComment(@PathVariable("blogId") Long blogId,
                             @Validated @RequestBody CommentPayload payload) {
        String content = payload.getContent();

        if (content == null || content.trim().isEmpty()) {
            return Result.fail("评论内容不能为空");
        }

        AccountProfile profile = ShiroUtil.getProfile();
        if (profile == null || profile.getId() == null) {
            return Result.fail(401, "用户未登录，无法发表评论", null);
        }

        if (blogService.getById(blogId) == null) {
            return Result.fail(404, "评论的博客不存在", null);
        }

        try {
            CommentDto newComment = commentService.addComment(blogId, content, profile.getId());
            return Result.succ("评论发表成功", newComment);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("发表评论时发生错误, blogId: {}, userId: {}", blogId, profile.getId(), e);
            return Result.fail("评论发表失败，请稍后再试");
        }
    }

    @Data
    static class CommentPayload {
        private String content;
    }


    @DeleteMapping("/comments/{commentId}")
    @RequiresAuthentication
    public Result deleteComment(@PathVariable("commentId") Long commentId) {
        AccountProfile profile = ShiroUtil.getProfile();
        if (profile == null || profile.getId() == null || profile.getRole() == null) {
            return Result.fail(401, "用户未登录或信息不全，无法删除评论", null);
        }

        try {
            boolean deleted = commentService.deleteComment(commentId, profile.getId(), profile.getRole());
            if (deleted) {
                return Result.succ("评论删除成功");
            } else {
                return Result.fail("评论删除失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.fail(404, e.getMessage(), null);
        } catch (UnauthorizedException e) {
            return Result.fail(403, e.getMessage(), null);
        } catch (Exception e) {
            log.error("删除评论时发生错误, commentId: {}, userId: {}", commentId, profile.getId(), e);
            return Result.fail("删除评论失败，请稍后再试");
        }
    }

    @GetMapping("/admin/comments/all")
    @RequiresRoles("admin")
    @RequiresAuthentication
    public Result listAllCommentsForAdmin(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String queryContent) {

        log.info("Admin request to list all comments. Page: {}, Size: {}, QueryContent: '{}'",
                currentPage, pageSize, queryContent);

        Page<CommentDto> page = new Page<>(currentPage, pageSize);
        IPage<CommentDto> commentPage = commentService.getAllCommentsAdmin(page, queryContent);

        return Result.succ(commentPage);
    }
}