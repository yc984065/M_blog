package com.mrblue.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mrblue.common.lang.Result;
import com.mrblue.entity.Blog;
import com.mrblue.entity.Category;
import com.mrblue.entity.Tag;
import com.mrblue.service.BlogService;
import com.mrblue.service.CategoryService;
import com.mrblue.service.TagService;
import com.mrblue.util.ShiroUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@RestController
public class BlogController {

    @Autowired
    BlogService blogService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    TagService tagService;

    @GetMapping("/blogs")
    public Result list(@RequestParam(defaultValue = "1") Integer currentPage, @RequestParam(defaultValue = "5") Integer pageSize) {

        Page<Blog> page = new Page<>(currentPage, pageSize);
        IPage<Blog> pageData = blogService.page(page, new QueryWrapper<Blog>().orderByDesc("created"));

        return Result.succ(pageData);
    }

    @GetMapping("/blog/{id}")
    public Result detail(@PathVariable(name = "id") Long id) {
        Blog blog = blogService.getById(id);
        Assert.notNull(blog, "该博客已被删除");

        // 加载分类和标签信息
        List<Category> categories = categoryService.findByBlogId(id);
        List<Tag> tags = tagService.findByBlogId(id);

        blog.setCategories(categories);  // 假设Blog有categories字段
        blog.setTags(tags);  // 假设Blog有tags字段

        return Result.succ(blog);
    }

    @PostMapping("/blog/edit")
    public Result edit(@Validated @RequestBody Blog blog) {
        Blog temp;

        if(blog.getId() != null) {
            temp = blogService.getById(blog.getId());
            checkPermission(temp);
        } else {
            temp = new Blog();
            temp.setUserId(ShiroUtil.getProfile().getId());
            temp.setCreated(LocalDateTime.now());
            temp.setStatus(0);
        }

        // 👇 插入这一段处理逻辑（兼容前端的 categoryIds 和 tagIds）
        if ((blog.getCategories() == null || blog.getCategories().isEmpty()) && blog.getCategoryIds() != null) {
            List<Category> categories = blog.getCategoryIds().stream()
                    .map(id -> {
                        Category c = new Category();
                        c.setId(id);
                        return c;
                    })
                    .collect(Collectors.toList());
            blog.setCategories(categories);
        }

        if ((blog.getTags() == null || blog.getTags().isEmpty()) && blog.getTagIds() != null) {
            List<Tag> tags = blog.getTagIds().stream()
                    .map(id -> {
                        Tag t = new Tag();
                        t.setId(id);
                        return t;
                    })
                    .collect(Collectors.toList());
            blog.setTags(tags);
        }

        // 复制普通字段
        BeanUtil.copyProperties(blog, temp, "id", "userId", "created", "status", "categories", "tags");


        blogService.saveOrUpdate(temp);

        // 保存分类和标签
        if (blog.getCategoryIds() != null) {
            categoryService.updateBlogCategories(temp.getId(), blog.getCategoryIds());
        }

        if (blog.getTagIds() != null) {
            tagService.updateBlogTags(temp.getId(), blog.getTagIds());
        }

        return Result.succ(null);
    }


    private void checkPermission(Blog blog) {
        Assert.notNull(blog, "博客不存在");
        Assert.isTrue(blog.getUserId().longValue() == ShiroUtil.getProfile().getId().longValue(), "没有权限编辑");
    }


    @DeleteMapping("/blog/{id}")
    public Result delete(@PathVariable(name = "id") Long id) {
        Blog blog = blogService.getById(id);
        if (blog == null) {
            return Result.fail("博客不存在");
        }

        blogService.removeById(id);
        return Result.succ(null);
    }

    // 新增搜索接口
    @GetMapping("/blogs/search")
    public Result search(@RequestParam(required = false) String query,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Long tagId) {
        List<Blog> blogs = blogService.searchBlogs(query, categoryId, tagId);
        return Result.succ(blogs);
    }


    @GetMapping("/categories")
    public Result listCategories() {
        return Result.succ(categoryService.list());
    }

    @GetMapping("/tags")
    public Result listTags() {
        return Result.succ(tagService.list());
    }

    @PostMapping("/blog/{id}/categories")
    @RequiresAuthentication
    public Result updateBlogCategories(
            @PathVariable Long id,
            @RequestBody List<Long> categoryIds
    ) {
        Blog blog = blogService.getById(id);
        Assert.notNull(blog, "该博客不存在");
        Assert.isTrue(blog.getUserId().equals(ShiroUtil.getProfile().getId()), "没有权限操作");

        categoryService.updateBlogCategories(id, categoryIds);
        return Result.succ(null);
    }

    @PostMapping("/blog/{id}/tags")
    @RequiresAuthentication
    public Result updateBlogTags(
            @PathVariable Long id,
            @RequestBody List<Long> tagIds
    ) {
        Blog blog = blogService.getById(id);
        Assert.notNull(blog, "该博客不存在");
        Assert.isTrue(blog.getUserId().equals(ShiroUtil.getProfile().getId()), "没有权限操作");

        tagService.updateBlogTags(id, tagIds);
        return Result.succ(null);
    }

}