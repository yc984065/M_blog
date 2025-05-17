package com.mrblue.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mrblue.common.dto.CommentDto; // 引入 DTO
import com.mrblue.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    // 自定义查询：获取评论列表并关联查询用户信息 (用户名、头像、角色)
    @Select("SELECT c.id, c.blog_id as blogId, c.user_id as userId, c.content, c.created_at as createdAt, " +
            "u.username, u.avatar as userAvatar, u.role as userRole " +
            "FROM m_comment c " +
            "JOIN m_user u ON c.user_id = u.id " +
            "WHERE c.blog_id = #{blogId} " +
            "ORDER BY c.created_at DESC")
    List<CommentDto> findCommentsByBlogIdWithUserDetails(@Param("blogId") Long blogId);

    // 管理员获取所有评论（可带内容搜索）
    @Select("SELECT c.id, c.blog_id as blogId, c.user_id as userId, c.content, c.created_at as createdAt, " +
            "u.username, u.avatar as userAvatar, u.role as userRole, b.title as blogTitle " +
            "FROM m_comment c " +
            "JOIN m_user u ON c.user_id = u.id " +
            "JOIN m_blog b ON c.blog_id = b.id " +
            " ${" + Constants.WRAPPER + ".customSqlSegment}")
    IPage<CommentDto> findAllCommentsAdmin(Page<CommentDto> page, @Param(Constants.WRAPPER) QueryWrapper<Comment> queryWrapper);

}