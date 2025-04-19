package com.mrblue.controller;


import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrblue.common.lang.Result;
import com.mrblue.entity.User;
import com.mrblue.service.UserService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @RequiresAuthentication
    @GetMapping("/index")
    public Result index() {
        User user = userService.getById(1L);
        return Result.succ(user);
    }

    @PostMapping("/save")
    public Result save(@Validated @RequestBody User user) {
        return Result.succ(user);
    }

    public static final String UPLOAD_DIR = "uploads/"; // 图片上传目录

    @PostMapping("/register")
    public Result register(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar
    ) {
        try {
            // 检查用户名是否已存在
            QueryWrapper<User> usernameWrapper = new QueryWrapper<>();
            usernameWrapper.eq("username", username);
            if (userService.getOne(usernameWrapper) != null) {
                return Result.fail("用户名已存在");
            }

            // 检查邮箱是否已存在
            QueryWrapper<User> emailWrapper = new QueryWrapper<>();
            emailWrapper.eq("email", email);
            if (userService.getOne(emailWrapper) != null) {
                return Result.fail("邮箱已存在");
            }

            // 创建用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(SecureUtil.md5(password)); // 使用 MD5 加密密码
            user.setEmail(email);
            user.setCreated(LocalDateTime.now());
            user.setStatus(0); // 默认状态为正常

            // 处理头像上传
            if (avatar != null && !avatar.isEmpty()) {
                // 确保上传目录存在
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    boolean created = uploadDir.mkdirs(); // 创建目录
                    if (!created) {
                        return Result.fail("无法创建上传目录");
                    }
                }

                // 生成唯一的文件名
                String fileName = UUID.randomUUID() + "_" + avatar.getOriginalFilename();
                String filePath = UPLOAD_DIR + fileName;

                // 保存文件
                avatar.transferTo(new File(filePath));

                // 设置用户头像路径
                user.setAvatar("/" + filePath);
            } else {
                // 设置默认头像
                user.setAvatar("/default-avatar.png");
            }

            // 保存用户
            userService.save(user);

            return Result.succ("注册成功");
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
            return Result.fail("注册失败：" + e.getMessage());
        }
    }

}
