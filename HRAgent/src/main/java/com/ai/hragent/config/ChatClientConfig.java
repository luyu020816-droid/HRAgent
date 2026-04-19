package com.ai.hragent.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.memory.redis.JedisRedisChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Autowired
    private JedisRedisChatMemoryRepository redisChatMemoryRepository;

    /**
     * 通义 ChatClient
     */
    @Bean
    public ChatClient qwenChatClient(ChatClient.Builder builder) {
        MessageWindowChatMemory memory = MessageWindowChatMemory
                .builder()
                .chatMemoryRepository(redisChatMemoryRepository) // 使用redis存储会话
                .maxMessages(10)
                .build();
        return builder
                .defaultAdvisors(
                        // 会话记录
                        MessageChatMemoryAdvisor.builder(memory).build()
                )
                .build();
    }

    /**
     * DeepSeek ChatClient
     */
    @Bean
    public ChatClient deepSeekChatClient(ChatClient.Builder builder) {
        MessageWindowChatMemory memory = MessageWindowChatMemory
                .builder()
                .chatMemoryRepository(redisChatMemoryRepository) // 使用redis存储会话
                .maxMessages(10)
                .build();
        return builder
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withModel("deepseek-v3.1")
                                .build())
                .defaultAdvisors(
                        // 会话记录
                        MessageChatMemoryAdvisor.builder(memory).build()
                )
                .build();
    }

}
