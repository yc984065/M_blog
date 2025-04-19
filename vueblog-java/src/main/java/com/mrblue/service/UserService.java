package com.mrblue.service;

import com.mrblue.common.dto.RegisterDto;
import com.mrblue.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;


public interface UserService extends IService<User> {
    boolean register(RegisterDto registerDto);
}
