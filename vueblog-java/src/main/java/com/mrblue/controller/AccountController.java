package com.mrblue.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrblue.common.dto.LoginDto;
import com.mrblue.common.dto.RegisterDto;
import com.mrblue.common.lang.Result;
import com.mrblue.entity.User;
import com.mrblue.service.UserService;
import com.mrblue.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
public class AccountController {

    @Autowired
    UserService userService;

    @Autowired
    JwtUtils jwtUtils;


    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response) {
        User user = userService.getOne(new QueryWrapper<User>().eq("username", loginDto.getUsername()));
        if (user == null) {
            return Result.fail("用户不存在");
        }
        String md5Password = SecureUtil.md5(loginDto.getPassword());
        if (!user.getPassword().equals(md5Password)) {
            return Result.fail("密码不正确");
        }
        String jwt = jwtUtils.generateToken(user.getId());
        response.setHeader("Authorization", jwt);
        response.setHeader("Access-control-Expose-Headers", "Authorization");
        return Result.succ(MapUtil.builder()
                .put("id", user.getId())
                .put("username", user.getUsername())
                .put("avatar", user.getAvatar())
                .put("email", user.getEmail())
                .map()
        );
    }

    @RequiresAuthentication
    @GetMapping("/logout")
    public Result logout() {
        SecurityUtils.getSubject().logout();
        return Result.succ(null);
    }

    @RequiresAuthentication
    @GetMapping("/refreshToken")
    public Result refreshToken(@RequestHeader("Authorization") String authorization) {
        String jwt = authorization.substring(7); // 移除 "Bearer " 前缀
        Claims claims = jwtUtils.getClaimByToken(jwt);
        if (claims == null) {
            return Result.fail("token无效");
        }
        if (jwtUtils.isTokenExpired(claims.getExpiration())) {
            return Result.fail("token已过期，请重新登录");
        }
        String newToken = jwtUtils.generateToken(Long.parseLong(claims.getSubject()));
        return Result.succ(newToken);
    }

    // 从配置文件中读取上传路径
    @Value("${file.upload.path}")
    private String uploadPath;

    @PostMapping("/register")
    public Result register(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar
    ) throws IOException {
        // 检查用户名和邮箱是否已存在
        if (userService.getOne(new QueryWrapper<User>().eq("username", username)) != null) {
            return Result.fail("用户名已存在");
        }
        if (userService.getOne(new QueryWrapper<User>().eq("email", email)) != null) {
            return Result.fail("邮箱已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(SecureUtil.md5(password));
        user.setEmail(email);
        user.setCreated(LocalDateTime.now());
        user.setStatus(0);

        // 处理头像上传
        if (avatar != null && !avatar.isEmpty()) {
            // 确保上传目录存在
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一的文件名
            String fileName = UUID.randomUUID() + "_" + avatar.getOriginalFilename();
            String filePath = uploadPath + File.separator + fileName; // 使用 File.separator 确保跨平台兼容性

            // 保存文件
            avatar.transferTo(new File(filePath));

            // 设置用户头像路径（只保存相对路径）
            user.setAvatar("/uploads/" + fileName); // 假设你的静态资源映射到 /uploads/
        } else {
            // 设置默认头像
            user.setAvatar("/default-avatar.png");
        }

        // 保存用户
        userService.save(user);

        return Result.succ("注册成功");
    }

}
