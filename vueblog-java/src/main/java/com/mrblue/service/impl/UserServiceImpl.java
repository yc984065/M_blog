package com.mrblue.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrblue.common.dto.RegisterDto;
import com.mrblue.entity.User;
import com.mrblue.mapper.UserMapper;
import com.mrblue.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public boolean register(RegisterDto registerDto) {
        // 检查用户名是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", registerDto.getUsername());
        if (this.getOne(wrapper) != null) {
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
        user.setStatus(0); // 默认状态为正常

        return this.save(user);
    }
}
