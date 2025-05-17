package com.mrblue.controller;

import com.mrblue.common.lang.Result;
import com.mrblue.entity.Blog;
import com.mrblue.service.BlogService;
import com.mrblue.service.CategoryService;
import com.mrblue.service.TagService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import java.util.List;

@Slf4j
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
    public Result listBlogsForUser(@RequestParam(defaultValue = "1") Integer currentPage,
                                   @RequestParam(defaultValue = "5") Integer pageSize) {
        // 普通用户查看列表时，不按任何查询词搜索，所以 query 参数为 null
        return Result.succ(blogService.getBlogsByPage(currentPage, pageSize, null));
    }

    @GetMapping("/blog/{id}")
    public Result getBlogDetail(@PathVariable Long id) {
        return Result.succ(blogService.getBlogWithRelations(id));
    }

    @PostMapping("/blog/edit")
    @RequiresAuthentication
    public Result createOrUpdateBlog(@Validated @RequestBody Blog blog) {
        try {
            blogService.saveOrUpdateBlog(blog);
            return Result.succ("操作成功");
        } catch (UnauthorizedException e) {
            log.warn("用户尝试编辑无权限的博客: {}", e.getMessage());
            return Result.fail(403, e.getMessage(), null);
        } catch (Exception e) {
            log.error("保存或更新博客失败: ", e);
            return Result.fail("操作失败，请稍后再试");
        }
    }

    @DeleteMapping("/blog/{id}")
    @RequiresAuthentication
    public Result deleteBlog(@PathVariable Long id) {
        try {
            blogService.removeById(id);
            return Result.succ("博客删除成功");
        } catch (IllegalArgumentException e) {
            log.warn("删除博客参数错误或博客不存在, blogId: {}: {}", id, e.getMessage());
            return Result.fail(e.getMessage());
        } catch (UnauthorizedException e) {
            log.warn("用户尝试删除无权限的博客, blogId: {}, Message: {}", id, e.getMessage());
            return Result.fail(403, "您没有权限删除该博客", null);
        } catch (Exception e) {
            log.error("删除博客时发生未知错误, blogId: {}", id, e);
            return Result.fail("删除博客失败，请稍后再试或联系管理员");
        }
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
        blogService.getBlogWithRelations(id);
        categoryService.updateBlogCategories(id, categoryIds);
        return Result.succ("分类更新成功");
    }

    @PostMapping("/blog/{id}/tags")
    @RequiresAuthentication
    public Result updateBlogTags(@PathVariable Long id,
                                 @RequestBody List<Long> tagIds) {
        blogService.getBlogWithRelations(id);
        tagService.updateBlogTags(id, tagIds);
        return Result.succ("标签更新成功");
    }

    //管理员获取所有博客列表的接口，将 title 参数改为 query
    @GetMapping("/admin/blogs/all")
    @RequiresRoles("admin")
    @RequiresAuthentication
    public Result listAllBlogsForAdmin(@RequestParam(defaultValue = "1") Integer currentPage,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String query) {
        log.info("Admin request to list all blogs. Page: {}, Size: {}, Query: '{}'", currentPage, pageSize, query);
        // 调用 Service 方法，传递 query 参数
        return Result.succ(blogService.getBlogsByPage(currentPage, pageSize, query));
    }
}