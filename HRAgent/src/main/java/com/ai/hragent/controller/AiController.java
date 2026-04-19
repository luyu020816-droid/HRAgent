package com.ai.hragent.controller;

import cn.hutool.core.util.ReUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.ai.hragent.entity.Resume;
import com.ai.hragent.service.IResumeService;
import com.ai.hragent.service.IUserService;
import com.ai.hragent.tools.Result;
import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import com.alibaba.cloud.ai.memory.redis.JedisRedisChatMemoryRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/aichat")
public class AiController {

    @Qualifier("deepSeekChatClient")
    @Autowired
    private ChatClient dsChatClient;
    @Qualifier("qwenChatClient")
    @Autowired
    private ChatClient chatClient;
    @Autowired
    private JedisRedisChatMemoryRepository redisChatMemoryRepository;
    @Autowired
    private IUserService userService;
    @Autowired
    private IResumeService resumeService;
    @Autowired
    private DashScopeApi dashScopeApi;
    private final static String FILE_PATH = "D:\\imgs\\data\\";

    @Value("classpath:/prompt-template/intention-prompt")
    private Resource intentionPrompt;
    // 2. 注入 Micrometer 记账本
    @Autowired
    private MeterRegistry meterRegistry;
    // 动态线程池
    @Autowired
    @Qualifier("dtpExecutor1")
    private DtpExecutor dtpExecutor;

    @RequestMapping("/dtp")
    public String dtp() {

        // 任务一：返回 "Task 1 result"
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
            try {
                // 模拟耗时操作
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return "Task 1 result";
        }, dtpExecutor);
        // 任务二：依赖任务一，返回 "Task 2 result" + 任务一的结果
        CompletableFuture<String> task2 = task1.handleAsync((result1, throwable) -> {
            try {
                // 模拟耗时操作
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return "Task 2 result " + result1;
        }, dtpExecutor);
        // 任务三：和任务一、任务二并行执行，返回 "Task 3 result"
        CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
            try {
                // 模拟耗时操作
                Thread.sleep(800); // 任务三可能比任务二先完成
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return "Task 3 result";
        }, dtpExecutor);
        // 任务四：依赖任务二和任务三，等待它们都完成后执行，返回 "Task 4 result" + 任务二和任务三的结果
        CompletableFuture<String> task4 = CompletableFuture.allOf(task2, task3)
                .handleAsync((res, throwable) -> {
                    try {
                        // 这里不需要显式等待，因为 allOf 已经保证了它们完成
                        return "Task 4 result with " + task2.get() + " and " + task3.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, dtpExecutor);
        // 获取任务四的结果并打印
        String finalResult = task4.join();
        System.out.println(finalResult);

        for (int i = 0; i < 10; i++) {
            dtpExecutor.submit(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return "dtp";
    }


    @PostMapping("/upload")
    public Result upload(@RequestPart("myfile") MultipartFile myfile) throws IOException {
        // 将文件进行存储 -> 如果要实现分布式存储：minIO、OSS
        String fileName = UUID.randomUUID().toString() + ".pdf";
        myfile.transferTo(new File(FILE_PATH + fileName));
        return Result.success(
                new HashMap<String, String>() {{
                    put("url", fileName);
                }}
        );
    }

    /**
     * 获取历史会话信息
     *
     * @param token
     * @return
     */
    @GetMapping("/getmessages")
    public Result getMessages(@RequestHeader("accesstoken") String token) {
        // 获取用户 ID
        long uid = userService.getUidByToken(token);
        return Result.success(redisChatMemoryRepository.findByConversationId("" + uid));
    }

    /**
     * 获取所有大模型
     *
     * @return
     */
    @RequestMapping("/models")
    public Result models() {
        return Result.success(new ArrayList() {{
            add(new Model("Qwen", "Qwen", true));
            add(new Model("DeepSeek", "DeepSeek", true));
        }});
    }

    /**
     * AI 对话功能
     *
     * @param msg
     * @return
     */

    @PostMapping("/sendmsg")
    public Result sendMsg(@RequestBody AiMsg msg, @RequestHeader("accesstoken") String token) {
        // 1.意图识别
        PromptTemplate template
                = new PromptTemplate(intentionPrompt);
        Prompt prompt = template.create(
                Map.of("query", msg.content)
        );
        String intention = chatClient
                .prompt(prompt)
                .call()
                .content();
        String res = "";
        switch (intention) {
            case "生成招聘信息":
                res = chat(msg.content, msg.model, userService.getUidByToken(token));
                break;
            case "公司规章制度查询":
                res = rules(msg.content, msg.model);
                break;
            case "简历入库":
                res = doResume(msg.files);
                break;
            default: // 聊天
                res = chat(msg.content, msg.model, userService.getUidByToken(token));
                break;
        }
        return Result.success(res);
    }

    /**
     * 简历入库
     * 1.读取pdf文件
     * 2.获取pdf中的简历关键信息 -> 姓名、年龄、手机号、邮箱...
     * 3.存储简历信息
     * 4.智能简历筛选功能（不符合简历要求的设置为未通过）
     *
     * @param files
     * @return
     */
//    private String doResume(List<MyFile> files) {
//        if (files != null && files.size() > 0) {
//            // 并发编程 -> 线程池/动态线程池/消息队列
//            files.forEach(file -> {
//                dtpExecutor.execute(() -> {
//                    // 读取 PDF 内容
//                    String content = loadPdfContent(file);
//                    // 提取 PDF 中的简历信息
//                    Resume resume = extractResumeInfo(content);
//                    // 简历筛选
//                    resume.setState(checkResume(resume));
//                    // 存储简历信息
//                    resumeService.save(resume);
//                    // 扩展：执行结果日志记录、回滚等操作...(暂时没做）
//                });
//            });
//        }
//        return "执行成功";
//    }
//    private String doResume(List<MyFile> files) {
//        if (files == null || files.isEmpty()) return "无文件处理";
//
//        // 1. 将每个文件的处理过程封装成 CompletableFuture
//        List<CompletableFuture<Void>> futures = files.stream()
//                .map(file -> CompletableFuture.runAsync(() -> {
//                    String content = loadPdfContent(file);
//                    Resume resume = extractResumeInfo(content);
//                    resume.setState(checkResume(resume));
//                    resumeService.save(resume);
//                }, dtpExecutor))
//                .toList();
//
//        // 2. 关键：等待所有任务完成（同步阻塞）
//        try {
//            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//        } catch (Exception e) {
//            return "部分简历处理失败：" + e.getMessage();
//        }
//
//        return "全部简历入库成功";
//    }

    /**
     * 串行
     */
    private String doResume(List<MyFile> files) {
        if (files == null || files.isEmpty()) return "无文件处理";

        try {
            // 直接使用增强 for 循环，主线程会按顺序一个一个处理
            for (MyFile file : files) {
                // 1. 读取 PDF 内容
                String content = loadPdfContent(file);

                // 2. 提取信息（建议确保此方法内也是纯串行，不要再用 CompletableFuture）
                Resume resume = extractResumeInfo(content);

                // 3. 筛选与保存
                resume.setState(checkResume(resume));
                resumeService.save(resume);

                // 打印日志方便你观察顺序执行
                System.out.println("串行处理完成文件: " + file.name());
            }
        } catch (Exception e) {
            return "简历处理过程中发生异常：" + e.getMessage();
        }

        return "全部简历串行入库成功";
    }

    /**
     * 简历筛选
     *
     * @param resume
     * @return
     */
    private int checkResume(Resume resume) {
        // 学历验证
        if (resume.getEdu().contains("专科") || resume.getEdu().contains("大专"))
            return -1;
        return 1;
    }

    /**
     * 从 PDF 中提取简历信息
     *
     * @param content
     * @return
     */
//    private Resume extractResumeInfo(String content) {
//        Resume resume = new Resume();
//
//        CompletableFuture<?> future = CompletableFuture.runAsync(() -> {
//            // 获取手机号
//            String phone = ReUtil.get("1[3-9]\\d{9}", content, 0);
//            resume.setPhone(phone != null ? phone : "");
//        }, dtpExecutor);
//
//        CompletableFuture<?> future2 = CompletableFuture.runAsync(() -> {
//            // 获取邮箱
//            String email = ReUtil.get("\\w+@\\w+(\\.\\w+)+", content, 0);
//            resume.setEmail(email != null ? email : "");
//        }, dtpExecutor);
//
//        CompletableFuture<?> future3 = CompletableFuture.runAsync(() -> {
//            // 获取姓名
//            String name = ReUtil.get("姓名[:：]\\s*([\\u4e00-\\u9fa5]{2,4})", content, 1);
//            resume.setName(name != null ? name : "");
//        }, dtpExecutor);
//
//        CompletableFuture<?> future4 = CompletableFuture.runAsync(() -> {
//            // 获取学历
//            String edu = ReUtil.get("教育经历[:：]\\s*(\\S+)", content, 1);
//            resume.setEdu(edu != null ? edu : "");
//        }, dtpExecutor);
//
//        CompletableFuture<?> future5 = CompletableFuture.runAsync(() -> {
//            // 年龄 - 安全解析
//            String ageStr = ReUtil.get("年龄[:：]\\s*(\\d{1,2})\\s*岁?", content, 1);
//            if (ageStr != null && !ageStr.isEmpty()) {
//                try {
//                    resume.setAge(Integer.parseInt(ageStr));
//                } catch (NumberFormatException e) {
//                    resume.setAge(0);
//                }
//            } else {
//                resume.setAge(0);
//            }
//        }, dtpExecutor);
//
//        CompletableFuture<?> future6 = CompletableFuture.runAsync(() -> {
//            // 工作年限
//            String workAge = ReUtil.get("工作年限[:：]\\s*(\\d+(\\.\\d+)?)\\s*年", content, 1);
//            resume.setWorkAge(workAge != null ? workAge : "");
//        }, dtpExecutor);
//
//        // 等待所有任务执行完
//        CompletableFuture.allOf(future, future2, future3, future4, future5, future6).join();
//        return resume;
//    }
    //串行执行
    private Resume extractResumeInfo(String content) {
        Resume resume = new Resume();

        // 1. 姓名：适配 "姓名： 陆禹"
        // 原正则 [\\u4e00-\\u9fa5] 有时会因为空格匹配失败，改为 \\S (非空字符)
        resume.setName(ReUtil.get("姓名[:：]\\s*(\\S{2,4})", content, 1));

        // 2. 手机号：通常比较准
        resume.setPhone(ReUtil.get("1[3-9]\\d{9}", content, 0));

        // 3. 邮箱
        resume.setEmail(ReUtil.get("\\w+@\\w+(\\.\\w+)+", content, 0));

        // 4. 学历：适配简历中的 "学历" 或 "教育经历"
        String edu = ReUtil.get("(?:学历|教育经历)[:：]\\s*(\\S+)", content, 1);
        resume.setEdu(edu);

        // 5. 年龄：最关键的报错点！适配 "年龄： 24" 或 "年龄： 24岁"
        // 增加防御性编程，防止 Integer.parseInt(null)
        String ageMatch = ReUtil.get("年龄[:：]\\s*(\\d{1,2})", content, 1);
        if (cn.hutool.core.util.NumberUtil.isNumber(ageMatch)) {
            resume.setAge(Integer.parseInt(ageMatch));
        } else {
            resume.setAge(0); // 默认值，确保程序不崩溃
        }

        // 6. 工作年限：适配 "工作年限： 5年" 或 "工作年限： 5"
        String workAge = ReUtil.get("工作年限[:：]\\s*(\\d+(\\.\\d+)?)", content, 1);
        resume.setWorkAge(workAge != null ? workAge : "0");

        return resume;
    }

    /**
     * 读取 PDF 文件
     *
     * @param file
     * @return
     */
    private String loadPdfContent(MyFile file) {
        // 获取文件
        File f = new File(FILE_PATH + file.url);
        // 读取pdf文件中内容
        try (PDDocument doc = PDDocument.load(f)) {
            PDFTextStripper stripper = new PDFTextStripper();
            // 读取到 PDF 中的内容
            String text = stripper.getText(doc);
            // 提取 PDF 中的关键信息（姓名、年龄、学历、邮箱...）
            return text;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 公司规章制度查询
     *
     * @param content
     * @param model
     * @return
     */
    private String rules(String content, String model) {
        DocumentRetriever retriever = new DashScopeDocumentRetriever(
                dashScopeApi,
                DashScopeDocumentRetrieverOptions
                        .builder()
                        .withIndexName("公司规章制度")
                        .build()
        );
        if (model.equals("DeepSeek")) {
            return dsChatClient.prompt()
                    .user(content)
                    .advisors(new DocumentRetrievalAdvisor(retriever))
                    .call()
                    .content();
        } else {
            return chatClient.prompt()
                    .user(content)
                    .advisors(new DocumentRetrievalAdvisor(retriever))
                    .call()
                    .content();
        }
    }

    /**
     * 聊天
     *
     * @param content
     * @return
     */
    private String chat(String content, String model, long uid) {
        ChatResponse response = (model.equals("DeepSeek") ? dsChatClient : chatClient)
                .prompt()
                .user(content)
                .advisors(a -> a.param(CONVERSATION_ID, uid))
                .call()
                .chatResponse();

        // 埋点记账
        recordTokenUsage(model, response);

        // 重点修改这里：使用 getText()
        if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
            return response.getResult().getOutput().getText();
        }
        return "AI 响应异常";
    }

    /**
     * 记录token消耗
     *
     * @param model
     * @param response
     */
    private void recordTokenUsage(String model, ChatResponse response) {
        if (response != null && response.getMetadata().getUsage() != null) {
            var usage = response.getMetadata().getUsage();

            // 记录 Prompt Tokens (输入)
            meterRegistry.counter("ai.token.usage",
                    "model", model,
                    "type", "prompt").increment(usage.getPromptTokens());

            // 记录 Completion Tokens (输出)
            meterRegistry.counter("ai.token.usage",
                    "model", model,
                    "type", "completion").increment(usage.getCompletionTokens());

            // 记录总消耗 (可选)
            meterRegistry.counter("ai.token.usage",
                    "model", model,
                    "type", "total").increment(usage.getTotalTokens());

            System.out.println("监控埋点成功：" + model + " 消耗 " + usage.getTotalTokens() + " tokens");
        }
    }

    /**
     * AI 助手传递的信息对象
     *
     * @param content
     * @param files
     * @param model
     */
    record AiMsg(String content, List<MyFile> files, String model) {
    }

    /**
     * 文件对象
     *
     * @param id
     * @param name
     * @param url
     */
    record MyFile(String id, String name, String url) {
    }

    /**
     * 大模型对象
     */
    record Model(String id, String name, boolean available) {
    }

}
