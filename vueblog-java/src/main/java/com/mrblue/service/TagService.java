package com.mrblue.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrblue.entity.Tag;
import java.util.List;

public interface TagService extends IService<Tag> {

    /**
     * 根据博客ID查询标签列表
     */
    List<Tag> findByBlogId(Long blogId);

    /**
     * 更新博客的标签关联
     */

    void updateBlogTags(Long blogId, List<Long> tagIds);

    /**
     * 创建或获取标签（如果标签已存在则直接返回）
     */
    Tag createOrGet(String tagName);

    /**
     * 根据名称查询标签
     */
    Tag findByName(String name);
}