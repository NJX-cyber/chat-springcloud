package com.example.agent.component;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * LLM 调用服务 — 封装 Resilience4j 熔断、超时降级
 *
 * @author normal
 * @date 2026/6/17
 */
@Component
public class LLMService {

    private static final Logger logger = LoggerFactory.getLogger(LLMService.class);

    @Resource
    private QwenChatModel qwenChatModel;

    /**
     * 带熔断 + 超时的 LLM 调用
     */
    @CircuitBreaker(name = "llm-chat", fallbackMethod = "chatFallback")
    @TimeLimiter(name = "llm-chat")
    public CompletableFuture<String> chat(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("LLM调用: prompt长度={}", prompt.length());
            return qwenChatModel.chat(prompt);
        });
    }

    /**
     * LLM 调用降级：返回一个安全的默认结果
     */
    @SuppressWarnings("unused")
    public CompletableFuture<String> chatFallback(String prompt, Throwable t) {
        logger.warn("LLM调用降级, 原因: {}", t.getMessage());
        return CompletableFuture.completedFuture("");
    }
}
