package com.example.agent.component;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在 add() 中完成 AI 回复优化后通知 Controller 取走结果，
 * 保证前端接收和数据库存储是同一份优化后文本。
 */
@Component
public class RefinedResponseHolder {

    private final ConcurrentHashMap<String, CompletableFuture<String>> futures = new ConcurrentHashMap<>();

    public CompletableFuture<String> register(String memoryId) {
        CompletableFuture<String> f = new CompletableFuture<>();
        futures.put(memoryId, f);
        return f;
    }

    public void complete(String memoryId, String refinedText) {
        CompletableFuture<String> f = futures.remove(memoryId);
        if (f != null) f.complete(refinedText);
    }
}
