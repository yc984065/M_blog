package com.mrblue.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mrblue.entity.Tag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface TagMapper extends BaseMapper<Tag> {

    // 查询某博客的所有标签
    @Select("SELECT t.* FROM tag t " +
            "JOIN blog_tag bt ON t.id = bt.tag_id " +
            "WHERE bt.blog_id = #{blogId}")
    List<Tag> findByBlogId(@Param("blogId") Long blogId);

    @Insert({
        "<script>",
                "INSERT INTO blog_tag (blog_id, tag_id) VALUES",
                "<foreach collection='tagIds' item='tagId' separator=','>",
                "(#{blogId}, #{tagId})",
                "</foreach>",
                "</script>"
    })
    void insertBlogTags(@Param("blogId") Long blogId, @Param("tagIds") List<Long> tagIds);

    @Delete("DELETE FROM blog_tag WHERE blog_id = #{blogId}")
    void deleteBlogTags(@Param("blogId") Long blogId);
}
