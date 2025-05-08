package com.mrblue.controller;

import com.mrblue.common.lang.Result;
import com.mrblue.entity.Blog;
import com.mrblue.service.BlogService;
import com.mrblue.service.CategoryService;
import com.mrblue.service.TagService;
import com.mrblue.util.ShiroUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BlogController {

    private final BlogService blogService;
    private final CategoryService categoryService;
    private final TagService tagService;

    @Autowired
    public BlogController(BlogService blogService,
                          CategoryService categoryService,
                          TagService tagService) {
        this.blogService = blogService;
        this.categoryService = categoryService;
        this.tagService = tagService;
    }

    @GetMapping("/blogs")
    public Result listBlogs(@RequestParam(defaultValue = "1") Integer currentPage,
                            @RequestParam(defaultValue = "5") Integer pageSize) {
        return Result.succ(blogService.getBlogsByPage(currentPage, pageSize));
    }

    @GetMapping("/blog/{id}")
    public Result getBlogDetail(@PathVariable Long id) {
        return Result.succ(blogService.getBlogWithRelations(id));
    }

    @PostMapping("/blog/edit")
    @RequiresAuthentication
    public Result createOrUpdateBlog(@Validated @RequestBody Blog blog) {
        blogService.saveOrUpdateBlog(blog);
        return Result.succ(null);
    }

    @DeleteMapping("/blog/{id}")
    @RequiresAuthentication
    public Result deleteBlog(@PathVariable Long id) {
        Blog blog = blogService.getById(id);
        if (blog == null) {
            return Result.fail("博客不存在");
        }
        checkPermission(blog);
        blogService.removeById(id);
        return Result.succ(null);
    }

    @GetMapping("/blogs/search")
    public Result searchBlogs(@RequestParam(required = false) String query,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) Long tagId) {
        return Result.succ(blogService.searchBlogsWithRelations(query, categoryId, tagId));
    }

    @GetMapping("/categories")
    public Result listAllCategories() {
        return Result.succ(categoryService.list());
    }

    @GetMapping("/tags")
    public Result listAllTags() {
        return Result.succ(tagService.list());
    }

    @PostMapping("/blog/{id}/categories")
    @RequiresAuthentication
    public Result updateBlogCategories(@PathVariable Long id,
                                       @RequestBody List<Long> categoryIds) {
        validateBlogOwnership(id);
        categoryService.updateBlogCategories(id, categoryIds);
        return Result.succ(null);
    }

    @PostMapping("/blog/{id}/tags")
    @RequiresAuthentication
    public Result updateBlogTags(@PathVariable Long id,
                                 @RequestBody List<Long> tagIds) {
        validateBlogOwnership(id);
        tagService.updateBlogTags(id, tagIds);
        return Result.succ(null);
    }

    private void checkPermission(Blog blog) {
        Assert.notNull(blog, "博客不存在");
        Assert.isTrue(blog.getUserId().longValue() == ShiroUtil.getProfile().getId().longValue(),
                "没有权限编辑");
    }

    private void validateBlogOwnership(Long blogId) {
        Blog blog = blogService.getById(blogId);
        Assert.notNull(blog, "该博客不存在");
        Assert.isTrue(blog.getUserId().equals(ShiroUtil.getProfile().getId()),
                "没有权限操作");
    }
}