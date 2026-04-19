package com.ai.hragent.mapper;

import com.ai.hragent.entity.Resume;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;

/**
 * <p>
 * 简历表 Mapper 接口
 * </p>
 *
 * @author leige
 * @since 2025-08-31
 */
public interface ResumeMapper extends BaseMapper<Resume> {

    /**
     * 简历人数统计
     *
     * @return
     */
    @Select("SELECT " +
            "    COUNT(*) AS total," +
            "    SUM(CASE WHEN state = 2 THEN 1 ELSE 0 END) AS interviewScheduled," +
            "    SUM(CASE WHEN state = -1 THEN 1 ELSE 0 END) AS rejected," +
            "    SUM(CASE WHEN state = 1 THEN 1 ELSE 0 END) AS pending" +
            " FROM resume;")
    HashMap<String, Integer> statistics();

    @Select("SELECT " +
            "    SUM(CASE WHEN state = 2 THEN 1 ELSE 0 END) AS oneIt," +
            "    SUM(CASE WHEN state = 3 THEN 1 ELSE 0 END) AS twoIt," +
            "    SUM(CASE WHEN state = 4 THEN 1 ELSE 0 END) AS threeIt" +
            " FROM resume;")
    HashMap<String, Integer> itStatistics();

}
