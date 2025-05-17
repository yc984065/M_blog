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
import com.mrblue.mapper.CategoryMapper;
import com.mrblue.mapper.TagMapper;
import com.mrblue.service.BlogService;
import com.mrblue.service.CategoryService;
import com.mrblue.service.KeywordService;
import com.mrblue.service.TagService;
import com.mrblue.shiro.AccountProfile;
import com.mrblue.util.ShiroUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    private final BlogMapper blogMapper;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final KeywordService keywordService;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    @Autowired
    public BlogServiceImpl(BlogMapper blogMapper,
                           @Lazy CategoryService categoryService,
                           @Lazy TagService tagService,
                           KeywordService keywordService,
                           CategoryMapper categoryMapper,
                           TagMapper tagMapper) {
        this.blogMapper = blogMapper;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.keywordService = keywordService;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
    }

    @Override
    public List<Blog> searchBlogs(String query, Long categoryId, Long tagId) {
        return blogMapper.searchBlogs(query, categoryId, tagId);
    }

    @Override
    public IPage<Blog> getBlogsByPage(Integer currentPage, Integer pageSize, String query) { // 参数名从 title 改为 query
        Page<Blog> page = new Page<>(currentPage, pageSize);
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(query)) {
            queryWrapper.like("title", query); // 对 "title" 字段进行模糊匹配
            log.debug("Fetching blogs for admin with title (query) like: {}", query);
        } else {
            log.debug("Fetching all blogs for admin (no query filter).");
        }

        queryWrapper.orderByDesc("created");
        return this.page(page, queryWrapper);
    }


    @Override
    public Blog getBlogWithRelations(Long id) {
        Blog blog = getById(id);
        Assert.notNull(blog, "该博客已被删除或不存在");
        List<Category> categories = categoryService.findByBlogId(id);
        List<Tag> tags = tagService.findByBlogId(id);
        blog.setCategories(categories);
        blog.setTags(tags);
        return blog;
    }

    @Override
    @Transactional
    public void saveOrUpdateBlog(Blog blog) {
        Blog temp;
        AccountProfile profile = ShiroUtil.getProfile();
        if (profile == null) {
            throw new UnauthorizedException("用户未登录，无法保存博客");
        }

        if (blog.getId() != null) {
            temp = getById(blog.getId());
            Assert.notNull(temp, "该博客不存在，无法更新");
            checkPermissionForEdit(temp, profile);
        } else {
            temp = new Blog();
            temp.setUserId(profile.getId());
            temp.setCreated(LocalDateTime.now());
            temp.setStatus(0);
        }

        processContentForSensitiveWords(blog);
        BeanUtil.copyProperties(blog, temp, "id", "userId", "created", "status", "categories", "tags", "categoryIds", "tagIds");

        boolean success = saveOrUpdate(temp);
        if (!success) {
            throw new RuntimeException("保存或更新博客主体失败");
        }
        updateBlogRelations(blog, temp.getId());
    }

    @Override
    @Transactional
    public boolean removeById(Serializable id) {
        Blog blog = getById(id);
        if (blog == null) {
            throw new IllegalArgumentException("博客不存在，无法删除");
        }
        AccountProfile profile = ShiroUtil.getProfile();
        if (profile == null) {
            throw new UnauthorizedException("用户未登录，无法执行删除操作");
        }
        boolean isAdmin = "admin".equals(profile.getRole());
        boolean isOwner = blog.getUserId().equals(profile.getId());

        if (isAdmin || isOwner) {
            categoryMapper.deleteBlogCategories((Long) id);
            tagMapper.deleteBlogTags((Long) id);
            boolean result = super.removeById(id);
            if (result) {
                log.info("博客 ID: {} 已成功删除", id);
            } else {
                log.warn("删除博客 ID: {} 失败（数据库操作返回false）", id);
                throw new RuntimeException("删除博客数据库操作失败");
            }
            return result;
        } else {
            log.warn("用户 {} (ID: {}) 尝试删除无权限的博客 ID: {} (作者ID: {})", profile.getUsername(), profile.getId(), id, blog.getUserId());
            throw new UnauthorizedException("您没有权限删除该文章");
        }
    }

    @Override
    public List<Blog> searchBlogsWithRelations(String query, Long categoryId, Long tagId) {
        List<Blog> blogs = blogMapper.searchBlogs(query, categoryId, tagId);
        if (blogs != null) {
            return blogs.stream()
                    .map(b -> getBlogWithRelations(b.getId()))
                    .distinct()
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private void checkPermissionForEdit(Blog blogToEdit, AccountProfile currentUser) {
        Assert.notNull(blogToEdit, "博客不存在");
        Assert.notNull(currentUser, "当前用户信息获取失败");
        boolean isAdmin = "admin".equals(currentUser.getRole());
        boolean isOwner = blogToEdit.getUserId().equals(currentUser.getId());

        if (!isOwner && !isAdmin) {
            log.warn("用户 {} (ID: {}) 尝试编辑无权限的博客 ID: {} (作者ID: {})",
                    currentUser.getUsername(), currentUser.getId(), blogToEdit.getId(), blogToEdit.getUserId());
            throw new UnauthorizedException("您没有权限编辑该文章");
        }
    }

    private void processContentForSensitiveWords(Blog blog) {
        if (blog.getTitle() != null) {
            blog.setTitle(keywordService.checkAndReplaceKeywords(blog.getTitle()));
        }
        if (blog.getDescription() != null) {
            blog.setDescription(keywordService.checkAndReplaceKeywords(blog.getDescription()));
        }
        if (blog.getContent() != null) {
            blog.setContent(keywordService.checkAndReplaceKeywords(blog.getContent()));
        }
    }

    private void updateBlogRelations(Blog blogViewModel, Long blogId) {
        if (blogViewModel.getCategoryIds() != null) {
            categoryService.updateBlogCategories(blogId, blogViewModel.getCategoryIds());
        } else {
            categoryService.updateBlogCategories(blogId, List.of());
        }
        if (blogViewModel.getTagIds() != null) {
            tagService.updateBlogTags(blogId, blogViewModel.getTagIds());
        } else {
            tagService.updateBlogTags(blogId, List.of());
        }
    }
}