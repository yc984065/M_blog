package com.mrblue.controller;

import com.mrblue.common.dto.RegisterDto;
import com.mrblue.common.lang.Result;
import com.mrblue.entity.User;
import com.mrblue.service.UserService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/register")
    public Result register(
            @Validated @ModelAttribute RegisterDto registerDto,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar
    ) {
        try {
            // 调用 UserService 的注册方法来处理注册逻辑，包括用户创建、密码加密、头像处理等
            userService.register(registerDto, avatar);
            return Result.succ("注册成功");
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage()); // 处理业务逻辑异常，例如用户名已存在
        } catch (Exception e) {
            e.printStackTrace(); // 打印其他异常信息
            return Result.fail("注册失败：" + e.getMessage());
        }
    }
}