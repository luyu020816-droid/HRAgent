package com.ai.hragent.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 面试表
 * </p>
 *
 * @author leige
 * @since 2025-09-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("interview")
public class Interview implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 简历投递id
     */
    private Integer rid;

    /**
     * 面试时间
     */
    private String interviewTime;

    /**
     * 面试官
     */
    private String interviewPerson;

    /**
     * 专业得分
     */
    private BigDecimal professionalScore;

    /**
     * 沟通得分
     */
    private BigDecimal communicationScore;

    /**
     * 团队协作得分
     */
    private BigDecimal teamworkScore;

    /**
     * 综合得分
     */
    private BigDecimal comprehensiveScore;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 备注
     */
    @TableField("`desc`")
    private String desc;

    /**
     * 评价
     */
    private String evaluate;

    private String result;


}
