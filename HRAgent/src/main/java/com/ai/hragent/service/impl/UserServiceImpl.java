package com.ai.hragent.service.impl;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.ai.hragent.entity.User;
import com.ai.hragent.mapper.UserMapper;
import com.ai.hragent.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author leige
 * @since 2025-08-21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public long getUidByToken(String token) {
        // 获取 token 用户信息
        JWT jwt = JWTUtil.parseToken(token);
        return Long.parseLong(jwt.getPayload("uid").toString());
    }
}
