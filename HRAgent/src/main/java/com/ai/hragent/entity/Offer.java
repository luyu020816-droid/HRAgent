package com.ai.hragent.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * Offer管理表
 * </p>
 *
 * @author leige
 * @since 2025-09-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("offer")
public class Offer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    private int rid;

    /**
     * 职位
     */
    private String position;

    /**
     * 部门
     */
    private String department;

    /**
     * 薪资
     */
    private BigDecimal salary;

    /**
     * 工作地点
     */
    private String workLocation;

    /**
     * 入职时间
     */
    private LocalDate onboardDate;

    /**
     * 负责HR名称
     */
    private String hrName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 修改时间
     */
    private LocalDateTime updatedTime;

    /**
     * 状态（5=待发Offer；6=已发Offer；-3=拒绝Offer）
     */
    private Integer status;

    private String remark;


}
