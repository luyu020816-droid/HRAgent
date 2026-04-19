package com.ai.hragent.controller;

import com.ai.hragent.entity.Interview;
import com.ai.hragent.entity.Offer;
import com.ai.hragent.entity.Resume;
import com.ai.hragent.service.IInterviewService;
import com.ai.hragent.service.IOfferService;
import com.ai.hragent.service.IResumeService;
import com.ai.hragent.tools.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/interview")
public class InterviewController {

    @Autowired
    private IResumeService resumeService;
    @Autowired
    private IInterviewService interviewService;
    @Autowired
    private IOfferService offerService;

    /**
     * 面试页面统计信息展示
     *
     * @return
     */
    @RequestMapping("/statistics")
    public Result statistics() {
        return Result.success(resumeService.itStatistics());
    }

    /**
     * @param interview
     * @return
     */
    @Transactional
    @RequestMapping("/add")
    public Result add(@RequestBody Interview interview) {
        // 1.面试评价表添加数据
        boolean result = interviewService.save(interview);
        if (result) {
            // 2.简历表面试官和面试时间冗余字段修改
            Resume resume = resumeService.getById(interview.getRid());
            resume.setInterviewPerson(interview.getInterviewPerson());
            resume.setInterviewTime(interview.getInterviewTime());
            result = resumeService.updateById(resume);
        }
        return result ? Result.success("success") : Result.fail(-1, "操作失败");
    }

    /**
     * 添加面试评价（修改操作）
     */
    @Transactional
    @RequestMapping("/update")
    public Result update(@RequestBody Interview interviewVO) {
        // 1.根据 interviewVO.getId() = rid 获取最新的面试评价记录
        QueryWrapper<Interview> itQueryWrapper = new QueryWrapper<>();
        itQueryWrapper.eq("rid", interviewVO.getId());
        itQueryWrapper.orderBy(true, false, "id");
        List<Interview> list = interviewService.list(itQueryWrapper);
        Interview interview = list.get(0);
        if (interview == null)
            return Result.fail(-1, "没有面试记录");
        // 2.面试评价表修改数据
        interview.setProfessionalScore(interviewVO.getProfessionalScore());
        interview.setCommunicationScore(interviewVO.getCommunicationScore());
        interview.setTeamworkScore(interviewVO.getTeamworkScore());
        interview.setComprehensiveScore(interviewVO.getComprehensiveScore());
        interview.setEvaluate(interviewVO.getEvaluate());
        interview.setResult(interviewVO.getResult());
        boolean result = interviewService.saveOrUpdate(interview);
        if (!result)
            return Result.fail(-1, "操作失败");
        // 3.简历投递表
        Resume resume = resumeService.getById(interview.getRid());
        if (resume == null) {
            // 事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.fail(-1, "操作失败");
        }
        //    3.1 将这一轮冗余面试官和面试时间进行删除
        resume.setInterviewPerson("");
        resume.setInterviewTime(null);
        //    3.2 修改状态
        if (!interviewVO.getResult().equals("PASSED")) {
            // 面试失败
            resume.setState(-2);
        } else {
            resume.setState(resume.getState() + 1);
        }
        result = resumeService.saveOrUpdate(resume);
        if (!result) {
            // 事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.fail(-1, "操作失败");
        }
        // 判断是否需要添加offer信息
        if (resume.getState() == 5) {
            Offer offer = new Offer();
            offer.setRid(resume.getId());
            offer.setName(resume.getName());
            offer.setEmail(resume.getEmail());
            offer.setStatus(resume.getState());
            result = offerService.save(offer);
            if (!result) {
                // 事务回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return Result.fail(-1, "操作失败");
            }
        }
        return Result.success("success");
    }

    /**
     * 根据简历id获取面试评价列表
     *
     * @param rid
     * @return
     */
    @RequestMapping("/getlistbyrid")
    public Result getListByRid(Integer rid) {
        if (rid == null || rid <= 0)
            return Result.fail(-1, "参数错误");
        QueryWrapper<Interview> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("rid", rid);
        List<Interview> list = interviewService.list(queryWrapper);
        return Result.success(list);
    }

}
