package com.mrblue.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mrblue.entity.Blog;
import com.mrblue.entity.Category;
import com.mrblue.entity.Tag;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface BlogMapper extends BaseMapper<Blog> {

    @Select({
            "<script>",
            "SELECT * FROM m_blog b",
            "JOIN blog_category bc ON b.id = bc.blog_id",
            "JOIN blog_tag bt ON b.id = bt.blog_id",
            "WHERE 1=1",
            "<if test='query != null and query.trim() != \"\"'>",
            "AND b.title LIKE CONCAT('%', #{query}, '%')",
            "</if>",
            "<if test='categoryId != null'>",
            "AND bc.category_id = #{categoryId}",
            "</if>",
            "<if test='tagId != null'>",
            "AND bt.tag_id = #{tagId}",
            "</if>",
            "ORDER BY b.created DESC",
            "</script>"
    })
    List<Blog> searchBlogs(@Param("query") String query,
                           @Param("categoryId") Long categoryId,
                           @Param("tagId") Long tagId);

    // 查询某博客对应的分类列表
    List<Category> findCategoriesByBlogId(@Param("blogId") Long blogId);

    // 查询某博客对应的标签列表
    List<Tag> findTagsByBlogId(@Param("blogId") Long blogId);
}
