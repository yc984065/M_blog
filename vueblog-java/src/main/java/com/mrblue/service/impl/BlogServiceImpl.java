package com.mrblue.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrblue.entity.Blog;
import com.mrblue.entity.Category;
import com.mrblue.entity.Tag;
import com.mrblue.mapper.BlogMapper;
import com.mrblue.service.BlogService;
import com.mrblue.service.CategoryService;
import com.mrblue.service.KeywordService;
import com.mrblue.service.TagService;
import com.mrblue.util.ShiroUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    private final BlogMapper blogMapper;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final KeywordService keywordService;

    @Autowired
    public BlogServiceImpl(BlogMapper blogMapper,
                           CategoryService categoryService,
                           TagService tagService,
                           KeywordService keywordService) {
        this.blogMapper = blogMapper;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.keywordService = keywordService;
    }

    @Override
    public List<Blog> searchBlogs(String query, Long categoryId, Long tagId) {
        return blogMapper.searchBlogs(query, categoryId, tagId);
    }

    @Override
    public IPage<Blog> getBlogsByPage(Integer currentPage, Integer pageSize) {
        Page<Blog> page = new Page<>(currentPage, pageSize);
        return page(page, new QueryWrapper<Blog>().orderByDesc("created"));
    }

    @Override
    public Blog getBlogWithRelations(Long id) {
        Blog blog = getById(id);
        Assert.notNull(blog, "该博客已被删除");

        List<Category> categories = categoryService.findByBlogId(id);
        List<Tag> tags = tagService.findByBlogId(id);

        blog.setCategories(categories);
        blog.setTags(tags);

        return blog;
    }

    @Override
    public void saveOrUpdateBlog(Blog blog) {
        Blog temp;

        if (blog.getId() != null) {
            temp = getById(blog.getId());
            checkPermission(temp);
        } else {
            temp = new Blog();
            temp.setUserId(ShiroUtil.getProfile().getId());
            temp.setCreated(LocalDateTime.now());
            temp.setStatus(0);
        }

        processCategoryAndTagIds(blog);
        BeanUtil.copyProperties(blog, temp, "id",
                "userId", "created", "status", "categories", "tags");
        processContentForSensitiveWords(temp);

        saveOrUpdate(temp);
        updateBlogRelations(blog, temp.getId());
    }

    @Override
    public List<Blog> searchBlogsWithRelations(String query, Long categoryId, Long tagId) {
        return searchBlogs(query, categoryId, tagId)
                .stream()
                .distinct()
                .peek(this::enrichBlogWithCategoryIds)
                .collect(Collectors.toList());
    }

    private void checkPermission(Blog blog) {
        Assert.notNull(blog, "博客不存在");
        Assert.isTrue(blog.getUserId().longValue() == ShiroUtil.getProfile().getId().longValue(),
                "没有权限编辑");
    }

    private void processCategoryAndTagIds(Blog blog) {
        if ((blog.getCategories() == null || blog.getCategories().isEmpty()) && blog.getCategoryIds() != null) {
            blog.setCategories(convertIdsToCategories(blog.getCategoryIds()));
        }

        if ((blog.getTags() == null || blog.getTags().isEmpty()) && blog.getTagIds() != null) {
            blog.setTags(convertIdsToTags(blog.getTagIds()));
        }
    }

    private List<Category> convertIdsToCategories(List<Long> ids) {
        return ids.stream()
                .map(id -> {
                    Category c = new Category();
                    c.setId(id);
                    return c;
                })
                .collect(Collectors.toList());
    }

    private List<Tag> convertIdsToTags(List<Long> ids) {
        return ids.stream()
                .map(id -> {
                    Tag t = new Tag();
                    t.setId(id);
                    return t;
                })
                .collect(Collectors.toList());
    }

    private void processContentForSensitiveWords(Blog blog) {
        blog.setTitle(keywordService.checkAndReplaceKeywords(blog.getTitle()));
        blog.setDescription(keywordService.checkAndReplaceKeywords(blog.getDescription()));
        blog.setContent(keywordService.checkAndReplaceKeywords(blog.getContent()));
    }

    private void updateBlogRelations(Blog blog, Long blogId) {
        if (blog.getCategoryIds() != null) {
            categoryService.updateBlogCategories(blogId, blog.getCategoryIds());
        }

        if (blog.getTagIds() != null) {
            tagService.updateBlogTags(blogId, blog.getTagIds());
        }
    }

    private void enrichBlogWithCategoryIds(Blog blog) {
        if (blog.getCategories() != null) {
            blog.setCategoryIds(
                    blog.getCategories().stream()
                            .map(Category::getId)
                            .collect(Collectors.toList())
            );
        }
    }
}