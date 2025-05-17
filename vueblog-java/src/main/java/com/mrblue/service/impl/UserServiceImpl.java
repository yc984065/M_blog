package com.mrblue.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.mrblue.common.dto.RegisterDto;
import com.mrblue.entity.User;
import com.mrblue.mapper.UserMapper;
import com.mrblue.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    public static final String UPLOAD_DIR = "uploads/"; // 图片上传目录

    @Override
    public boolean register(RegisterDto registerDto, MultipartFile avatar) {
        // 检查用户名是否已存在
        QueryWrapper<User> usernameWrapper = new QueryWrapper<>();
        usernameWrapper.eq("username", registerDto.getUsername());
        if (this.getOne(usernameWrapper) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 检查邮箱是否已存在
        QueryWrapper<User> emailWrapper = new QueryWrapper<>();
        emailWrapper.eq("email", registerDto.getEmail());
        if (this.getOne(emailWrapper) != null) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(SecureUtil.md5(registerDto.getPassword())); // 使用 MD5 加密密码
        user.setEmail(registerDto.getEmail());
        user.setCreated(LocalDateTime.now());
        user.setRole(registerDto.getRole());
        user.setStatus(0); // 默认状态为正常

        // 处理头像上传
        if (avatar != null && !avatar.isEmpty()) {
            try {
                // 确保上传目录存在
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    boolean created = uploadDir.mkdirs(); // 创建目录
                    if (!created) {
                        throw new RuntimeException("无法创建上传目录");
                    }
                }

                // 生成唯一的文件名
                String fileName = UUID.randomUUID() + "_" + avatar.getOriginalFilename();
                String filePath = UPLOAD_DIR + fileName;

                // 保存文件
                avatar.transferTo(new File(filePath));

                // 设置用户头像路径
                user.setAvatar("/" + filePath);
            } catch (IOException e) {
                throw new RuntimeException("头像上传失败：" + e.getMessage());
            }
        } else {
            // 设置默认头像
            user.setAvatar("/default-avatar.png");
        }

        return this.save(user);
    }
}