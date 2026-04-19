package com.ai.hragent.controller;


import com.ai.hragent.entity.Offer;
import com.ai.hragent.entity.Resume;
import com.ai.hragent.service.IOfferService;
import com.ai.hragent.service.IResumeService;
import com.ai.hragent.tools.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * <p>
 * Offer管理表 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/offer")
public class OfferController {

    @Autowired
    private IOfferService offerService;
    @Autowired
    private IResumeService resumeService;
    @Autowired
    private ChatClient qwenChatClient;
    @Value("classpath:/prompt-template/offer-prompt")
    private Resource offerPrompt;
    @Autowired
    private MailSender mailSender;
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 查询 Offer 列表
     *
     * @param params
     * @return
     */
    @RequestMapping("/list")
    public Result list(@RequestBody ListParams params) {
        Page page = new Page(params.pageNo, params.pageSize);
        QueryWrapper<Offer> queryWrapper = new QueryWrapper<>();
        if (!params.name.isEmpty()) {
            queryWrapper.like("name", params.name);
        }
        if (!params.status.isEmpty()) {
            queryWrapper.eq("status", params.status);
        }
        return Result.success(offerService.page(page, queryWrapper));
    }

    /**
     * Offer 修改
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Offer offer) {
        return offerService.updateById(offer) ? Result.success("success") : Result.fail(-1, "操作失败");
    }

    /**
     * 发送邮件
     *
     * @param offer 只传递 id
     * @return
     */
    @Transactional
    @RequestMapping("/sendemail")
    public Result sendEmail(@RequestBody Offer offer) {
        if (offer.getId() == null || offer.getId() <= 0)
            return Result.fail(-1, "参数错误！");
        offer = offerService.getById(offer.getId());
        if (offer == null)
            return Result.fail(-1, "参数错误！");
        // 校验用户状态
        if (offer.getStatus() != 5)
            return Result.fail(-2, "当前是无效用户状态！");
        // 使用 AI 生成邮件信息
        PromptTemplate promptTemplate = new PromptTemplate(offerPrompt);
        Prompt prompt = promptTemplate.create(Map.of(
                "name", offer.getName(),
                "department", offer.getDepartment(),
                "position", offer.getPosition(),
                "workLocation", offer.getWorkLocation(),
                "salary", offer.getSalary(),
                "onboardDate", offer.getOnboardDate()
        ));
        // AI 生成的邮件信息
        String emailContent = qwenChatClient.prompt(prompt).call().content();
        System.out.println(emailContent);
        // 发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from); // 发件人
        message.setTo(offer.getEmail());
        message.setSubject("入职offer");
        message.setText(emailContent);
        mailSender.send(message);
        // 修改用户状态（两张表）
        // 修改offer表状态
        offer.setStatus(6);
        boolean result = offerService.saveOrUpdate(offer);
        if (!result) {
            // 事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.fail(-1, "操作失败");
        }
        // 修改resume表状态
        Resume resume = resumeService.getById(offer.getRid());
        if (resume == null) {
            // 事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.fail(-1, "操作失败");
        }
        resume.setState(offer.getStatus());
        result = resumeService.saveOrUpdate(resume);
        if (!result) {
            // 事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.fail(-1, "操作失败");
        }
        return Result.success("Offer已发放！");
    }

    /**
     * 撤回 Offer
     *
     * @param offer
     * @return
     */
    @Transactional
    @RequestMapping("/rollback")
    public Result rollback(@RequestBody Offer offer) {
        if (offer.getId() == null || offer.getId() <= 0)
            return Result.fail(-1, "参数错误！");
        offer = offerService.getById(offer.getId());
        if (offer == null || offer.getStatus() != 6)
            return Result.fail(-1, "参数错误！");
        // 更改 offer 表的状态
        offer.setStatus(-3);
        boolean result = offerService.saveOrUpdate(offer);
        if (!result) {
            // 事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.fail(-1, "操作失败");
        }
        // 更正 resume 表的状态
        Resume resume = resumeService.getById(offer.getRid());
        if (resume == null || resume.getState() != 6) {
            // 事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.fail(-1, "操作失败");
        }
        resume.setState(-3);
        result = resumeService.saveOrUpdate(resume);
        if (!result) {
            // 事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.fail(-1, "操作失败");
        }
        return Result.success("Offer已撤回");
    }

    record ListParams(Integer pageNo, Integer pageSize, String name, String status) {
    }

}
