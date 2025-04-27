package com.mrblue.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mrblue.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    // 查询某博客的所有分类
    @Select("SELECT c.* FROM category c " +
            "JOIN blog_category bc ON c.id = bc.category_id " +
            "WHERE bc.blog_id = #{blogId}")
    List<Category> findByBlogId(@Param("blogId") Long blogId);

    // 插入多个分类关系
    @Insert({
            "<script>",
            "INSERT INTO blog_category (blog_id, category_id) VALUES ",
            "<foreach collection='categoryIds' item='categoryId' separator=','>",
            "(#{blogId}, #{categoryId})",
            "</foreach>",
            "</script>"
    })
    void insertBlogCategories(@Param("blogId") Long blogId, @Param("categoryIds") List<Long> categoryIds);

    // 删除博客对应的所有分类关系
    @Delete("DELETE FROM blog_category WHERE blog_id = #{blogId}")
    void deleteBlogCategories(@Param("blogId") Long blogId);
}
