package com.mrblue.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrblue.common.dto.RegisterDto;
import com.mrblue.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {

    boolean register(RegisterDto registerDto, MultipartFile avatar);
}