package com.example.agent.config;

import com.example.agent.component.RefinedResponseHolder;
import com.example.agent.memory.SummarizationService;
import com.example.agent.memory.SummarizingChatMemory;
import com.example.agent.store.MongoChatMemoryStore;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * author:normal
 * date:2026/5/29 10:41
 * description:
 */
@Configuration
public class ChatAgentConfig {

    @Resource
    private MongoChatMemoryStore mongoChatMemoryStore;

    @Resource
    private RedisEmbeddingStore redisEmbeddingStore;

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private SummarizationService summarizationService;

    @Resource
    private RefinedResponseHolder refinedResponseHolder;

    @Bean
    public ChatMemoryProvider chatMemoryProviderAgent() {
        String systemPrompt = loadSystemPrompt();
        return memoryId -> {
            var baseMemory = MessageWindowChatMemory.builder()
                    .id(memoryId)
                    .maxMessages(200)
                    .chatMemoryStore(mongoChatMemoryStore)
                    .build();
            return new SummarizingChatMemory(baseMemory, summarizationService, systemPrompt,
                    refinedResponseHolder);
        };
    }

    @Bean
    public ContentRetriever contentRetrieverAgent() {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(redisEmbeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.6)
                .build();
    }

    private String loadSystemPrompt() {
        try (var in = getClass().getClassLoader().getResourceAsStream("xiaozhi.txt")) {
            if (in == null) return "";
            String template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return template.replace("{{current_date}}", LocalDate.now().toString());
        } catch (IOException e) {
            throw new RuntimeException("无法加载系统提示词 xiaozhi.txt", e);
        }
    }
}
