package com.ai.hragent.service;

import com.ai.hragent.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author leige
 * @since 2025-08-21
 */
public interface IUserService extends IService<User> {

    long getUidByToken(String token);

}
