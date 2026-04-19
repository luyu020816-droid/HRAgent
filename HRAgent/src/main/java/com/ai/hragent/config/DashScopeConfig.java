package com.ai.hragent.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.dashscope.protocol.ConnectionConfigurations;
import com.alibaba.dashscope.utils.Constants;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 百炼平台配置信息
 */
@Configuration
public class DashScopeConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Bean
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder()
                .apiKey(apiKey)
                .build();
    }

    @PostConstruct
    public void initDashScope() {
        // 注意：确保 Constants 的导入语句没有报错
        Constants.connectionConfigurations = ConnectionConfigurations.builder()
                .connectTimeout(Duration.ofSeconds(60))   // 连接超时：60秒
                .readTimeout(Duration.ofSeconds(300))     // 读取超时：5分钟，根据你的任务需要可以调整
                .writeTimeout(Duration.ofSeconds(60))     // 写入超时：60秒
                .connectionIdleTimeout(Duration.ofSeconds(300)) // 空闲连接存活时间
                .connectionPoolSize(256)                  // 连接池大小，高并发场景可调大
                .build();

        // 可以加一行日志，确认配置类被加载
        System.out.println("DashScope SDK 超时配置已初始化，readTimeout: 300秒");
    }
}
