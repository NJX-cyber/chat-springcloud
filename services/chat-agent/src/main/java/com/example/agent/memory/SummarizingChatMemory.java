package com.example.agent.memory;

import com.example.agent.component.RefinedResponseHolder;
import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.ChatMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 带对话摘要 + RAG 过滤 + AI 回复质量优化的 ChatMemory 包装器。
 *
 * @author normal
 * @date 2026/6/17
 */
public class SummarizingChatMemory implements ChatMemory {

    private static final Logger logger = LoggerFactory.getLogger(SummarizingChatMemory.class);

    private static final int MAX_RECENT_MESSAGES = 10;
    private static final int SUMMARY_TRIGGER = 20;
    private static final String RAG_MARKER = "Answer using the following information:";

    private final ChatMemory delegate;
    private final SummarizationService summarizationService;
    private final String systemPrompt;
    private final RefinedResponseHolder refinedHolder;
    private final ConcurrentMap<String, CachedSummary> cache = new ConcurrentHashMap<>();

    private String lastUserText;

    public SummarizingChatMemory(ChatMemory delegate,
                                  SummarizationService summarizationService,
                                  String systemPrompt,
                                  RefinedResponseHolder refinedHolder) {
        this.delegate = delegate;
        this.summarizationService = summarizationService;
        this.systemPrompt = systemPrompt;
        this.refinedHolder = refinedHolder;
    }

    @Override
    public Object id() { return delegate.id(); }

    @Override
    public void add(ChatMessage message) {
        if (message instanceof SystemMessage) return;

        if (message instanceof UserMessage userMsg) {
            lastUserText = extractText(userMsg);
            delegate.add(stripRag(userMsg));
            return;
        }

        if (message instanceof AiMessage aiMsg && !aiMsg.hasToolExecutionRequests()
                && lastUserText != null) {
            String refined = summarizationService.refineResponse(lastUserText, aiMsg.text());
            refinedHolder.complete(delegate.id().toString(), refined);
            if (!refined.equals(aiMsg.text())) {
                message = new AiMessage(refined);
            }
        }

        delegate.add(message);
    }

    private String extractText(UserMessage userMsg) {
        for (Content c : userMsg.contents()) {
            if (c instanceof TextContent tc) {
                return tc.text();
            }
        }
        return "";
    }

    private ChatMessage stripRag(UserMessage userMsg) {
        List<Content> contents = userMsg.contents();
        List<Content> cleaned = new ArrayList<>(contents.size());
        boolean modified = false;

        for (Content c : contents) {
            if (!(c instanceof TextContent tc)) {
                cleaned.add(c);
                continue;
            }
            String text = tc.text();
            int idx = text.indexOf(RAG_MARKER);
            if (idx > 0) {
                cleaned.add(new TextContent(text.substring(0, idx).stripTrailing()));
                modified = true;
                logger.debug("已移除 RAG 追加内容, 原始长度={}, 截断后长度={}", text.length(), idx);
            } else {
                cleaned.add(c);
            }
        }
        return modified ? new UserMessage(cleaned) : userMsg;
    }

    @Override
    public List<ChatMessage> messages() {
        List<ChatMessage> all = new ArrayList<>(delegate.messages());

        List<ChatMessage> sysMessages = new ArrayList<>();
        List<ChatMessage> dialogMessages = new ArrayList<>();

        for (ChatMessage msg : all) {
            if (msg instanceof SystemMessage) sysMessages.add(msg);
            else dialogMessages.add(msg);
        }

        List<ChatMessage> fullSys = new ArrayList<>();
        fullSys.add(new SystemMessage(systemPrompt));
        fullSys.addAll(sysMessages);

        if (dialogMessages.isEmpty()) {
            return new ArrayList<>(fullSys);
        }

        int total = dialogMessages.size();
        if (total < SUMMARY_TRIGGER) {
            List<ChatMessage> result = new ArrayList<>(fullSys);
            result.addAll(dialogMessages);
            return result;
        }

        String memId = delegate.id().toString();

        if (total % SUMMARY_TRIGGER == 0) {
            int recentStart = Math.max(0, total - MAX_RECENT_MESSAGES);
            List<ChatMessage> toSummarize = dialogMessages.subList(0, recentStart);
            List<ChatMessage> recent = dialogMessages.subList(recentStart, total);

            try {
                String summary = summarizationService.summarize(new ArrayList<>(toSummarize));
                cache.put(memId, new CachedSummary(summary, total));
                return buildResult(fullSys, summary, recent);
            } catch (Exception e) {
                logger.warn("对话摘要生成失败: {}", e.getMessage());
                return buildResult(fullSys, null, recent);
            }
        }

        CachedSummary cached = cache.get(memId);
        String summary = cached != null ? cached.summary : null;
        return buildResult(fullSys, summary, dialogMessages);
    }

    @Override
    public void clear() {
        delegate.clear();
        cache.remove(delegate.id().toString());
        logger.debug("已清空对话记忆, memoryId={}", delegate.id());
    }

    private List<ChatMessage> buildResult(List<ChatMessage> sysMessages,
                                           String summary,
                                           List<ChatMessage> recent) {
        List<ChatMessage> result = new ArrayList<>(sysMessages);
        if (summary != null && !summary.isBlank()) {
            result.add(new SystemMessage("[历史对话摘要] " + summary));
        }
        result.addAll(recent);
        return result;
    }

    private record CachedSummary(String summary, int msgCount) {}
}
