package com.mrblue.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrblue.entity.BlogTag;
import com.mrblue.entity.Tag;
import com.mrblue.mapper.BlogTagMapper;
import com.mrblue.mapper.TagMapper;
import com.mrblue.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Autowired
    private BlogTagMapper blogTagMapper;

    @Autowired
    private TagMapper tagMapper;

    @Override
    public List<Tag> findByBlogId(Long blogId) {
        return tagMapper.findByBlogId(blogId);
    }

    @Override
    public void updateBlogTags(Long blogId, List<Long> tagId) {
        // 先删除旧的，再添加新的
        tagMapper.deleteBlogTags(blogId);
        if (tagId != null && !tagId.isEmpty()) {
            tagMapper.insertBlogTags(blogId, tagId);
        }
    }

    @Override
    @Transactional
    public Tag createOrGet(String tagName) {
        Tag existTag = this.findByName(tagName);
        if (existTag != null) {
            return existTag;
        }

        Tag newTag = new Tag();
        newTag.setName(tagName);
        newTag.setColor(generateRandomColor());
        this.save(newTag);
        return newTag;
    }

    @Override
    public Tag findByName(String name) {
        return this.getOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Tag>().eq("name", name));
    }

    /**
     * 生成随机颜色（十六进制）
     */
    private String generateRandomColor() {
        String[] colors = {
                "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A",
                "#98D8C8", "#F06292", "#7986CB", "#9575CD"
        };
        return colors[new Random().nextInt(colors.length)];
    }

}
