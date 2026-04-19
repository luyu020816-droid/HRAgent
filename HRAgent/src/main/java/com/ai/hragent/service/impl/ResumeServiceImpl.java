package com.ai.hragent.service.impl;

import com.ai.hragent.entity.Resume;
import com.ai.hragent.mapper.ResumeMapper;
import com.ai.hragent.service.IResumeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * <p>
 * 简历表 服务实现类
 * </p>
 *
 * @author leige
 * @since 2025-08-31
 */
@Service
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements IResumeService {

    @Autowired
    private ResumeMapper resumeMapper;

    @Override
    public HashMap<String, Integer> statistics() {
        return resumeMapper.statistics();
    }

    @Override
    public HashMap<String, Integer> itStatistics() {
        return resumeMapper.itStatistics();
    }

}
