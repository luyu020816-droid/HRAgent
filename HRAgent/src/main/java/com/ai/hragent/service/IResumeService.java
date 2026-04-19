package com.ai.hragent.service;

import com.ai.hragent.entity.Resume;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.HashMap;

/**
 * <p>
 * 简历表 服务类
 * </p>
 *
 * @author leige
 * @since 2025-08-31
 */
public interface IResumeService extends IService<Resume> {

    HashMap<String, Integer> statistics();

    HashMap<String, Integer> itStatistics();

}
