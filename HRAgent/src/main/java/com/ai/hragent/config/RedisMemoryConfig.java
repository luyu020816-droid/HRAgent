package com.ai.hragent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.alibaba.cloud.ai.memory.redis.JedisRedisChatMemoryRepository;

/**
 * 将大模型连续对话功能存储到 Redis 配置信息
 */
@Configuration
public class RedisMemoryConfig {

    @Value("${spring.ai.memory.redis.host}")
    private String host;
    @Value("${spring.ai.memory.redis.port}")
    private Integer port;

    @Bean
    public JedisRedisChatMemoryRepository redisChatMemoryRepository() {
        return JedisRedisChatMemoryRepository.builder()
                .host(host)
                .port(port)
                .build();
    }
}
