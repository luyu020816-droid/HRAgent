package com.ai.hragent.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历表
 */
@Data
public class Resume {
    private int id;
    private String name;
    private String phone;
    private String email;
    private String workAge;
    private String edu;
    private int age;
    // -3=拒绝Offer；-2=面试未通过；-1=简历未通过筛选；1=待处理；2=通过筛选/面试中/一面；3=二面；4=三面；
    // 5=待发Offer；6=已发Offer/入职
    private int state;
    private LocalDateTime createTime;
    private String url;
    @TableField("`desc`")
    private String desc;
    // 面试官
    private String interviewPerson;
    // 面试时间
    private String interviewTime;
}
