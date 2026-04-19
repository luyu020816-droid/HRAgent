package com.ai.hragent.controller;


import cn.hutool.core.convert.Convert;
import com.ai.hragent.entity.Resume;
import com.ai.hragent.service.IResumeService;
import com.ai.hragent.tools.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 简历表 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/resume")
public class ResumeController {

    @Autowired
    private IResumeService resumeService;

    /**
     * 获取简历列表
     *
     * @param params
     * @return
     */
    @RequestMapping("/list")
    public Result list(@RequestBody ResumeListParams params) {
        QueryWrapper<Resume> queryWrapper = new QueryWrapper<>();
        if (params.name != null && !params.name.isEmpty()) {
            queryWrapper.like("name", params.name);
        }
        if (params.phone != null && !params.phone.isEmpty()) {
            queryWrapper.like("phone", params.phone);
        }
        if (params.email != null && !params.email.isEmpty()) {
            queryWrapper.like("email", params.email);
        }
        // 查询条件的状态
        if (params.status != 0) {
            queryWrapper.eq("state", params.status);
        } else if (params.baseStatus != null && !params.baseStatus.isEmpty()) {
            // 列表（基础）状态设置
            queryWrapper.in("state", params.baseStatus);
        }
        Page page = new Page(params.pageNo, params.pageSize);
        return Result.success(resumeService.page(page, queryWrapper));
    }

    /**
     * 获取简历统计数量信息
     *
     * @return
     */
    @RequestMapping("/statistics")
    public Result statistics() {
        return Result.success(resumeService.statistics());
    }

    /**
     * 手动新增简历
     *
     * @param resume
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Resume resume) {
        // todo：关键字段非空验证
        return resumeService.save(resume) ? Result.success("success") : Result.fail(-1, "操作失败");
    }

    /**
     * 简历修改
     *
     * @param resume
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Resume resume) {
        // 关键字段非空验证
        if (resume.getId() <= 0)
            return Result.fail(-1, "id不能为空");
        return resumeService.updateById(resume) ? Result.success("success") : Result.fail(-1, "操作失败");
    }

    /**
     * 删除简历
     */
    @RequestMapping("/delete")
    public Result delete(@RequestBody Resume resume) {
        if (resume.getId() <= 0)
            return Result.fail(-1, "id不能为空");
        return resumeService.removeById(resume.getId()) ? Result.success("success") : Result.fail(-1, "操作失败");
    }

    @RequestMapping("/deletes")
    public Result deletes(@RequestBody IdsParams params) {
        return resumeService.removeByIds(
                Convert.toList(Integer.class, params.ids)) ? Result.success("success") : Result.fail(-1, "操作失败");
    }

    record IdsParams(int[] ids) {
    }

    record ResumeListParams(int pageNo, int pageSize, String name,
                            int status, List<Integer> baseStatus,
                            String phone, String email) {
    }

}
