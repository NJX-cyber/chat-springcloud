package com.example.agent.service.impl;

import com.example.agent.bean.vo.MessageVO;
import com.example.agent.service.ChatMessageService;
import com.example.agent.component.LLMService;
import com.example.agent.store.MongoChatMemoryStore;
import com.example.agent.utils.Utils;
import com.example.model.enums.ResponseCodeEnum;
import com.example.model.exception.BusinessException;
import dev.langchain4j.data.message.*;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * author:normal
 * date:2026/5/30 9:18
 * description:
 */
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageServiceImpl.class);

    @Resource
    private MongoChatMemoryStore mongoChatMemoryStore;

    @Resource
    private LLMService llmService;

    @Override
    public List<MessageVO> getHistory(String memoryId) {
        List<ChatMessage> historyList = mongoChatMemoryStore.getMessages(memoryId);

        List<MessageVO> voList = new ArrayList<>();
        for (ChatMessage msg : historyList) {
            // 只保留用户消息和AI文本回复，跳过工具调用和工具结果
            if (msg instanceof AiMessage aiMsg) {
                // 跳过包含工具调用的AI消息（如toolExecutionRequests）
                if (aiMsg.hasToolExecutionRequests()) {
                    logger.debug("跳过工具调用消息");
                    continue;
                }
                String text = aiMsg.text();
                if (text == null || text.isBlank()) {
                    logger.debug("跳过空文本AI消息");
                    continue;
                }
                MessageVO vo = new MessageVO();
                vo.setSendUserId("Urobot");
                vo.setMessageContent(text);
                voList.add(vo);
            } else if (msg instanceof UserMessage) {
                String text = Utils.getContent(msg);
                if (text == null || text.isBlank()) {
                    continue;
                }
                MessageVO vo = new MessageVO();
                vo.setSendUserId(memoryId);
                vo.setMessageContent(text);
                voList.add(vo);
            }
            // 跳过 ToolExecutionResultMessage、SystemMessage 等其他类型
        }
        return voList;
    }

    @Override
    @Async
    public CompletableFuture<String> getTitle(String memoryId) {
        List<ChatMessage> historyList = mongoChatMemoryStore.getMessages(memoryId);
        if (historyList == null || historyList.isEmpty()) {
            return CompletableFuture.completedFuture("新对话");
        }
        // 找到第一条用户消息
        String userContent = "";
        for (ChatMessage msg : historyList) {
            if (msg instanceof UserMessage) {
                userContent = Utils.getContent(msg);
                break;
            }
        }
        if (userContent.isBlank()) {
            return CompletableFuture.completedFuture("新对话");
        }

        String prompt = String.format(
                "请根据以下对话内容，生成一个4-8个字的简短标题。直接输出标题，不要标点符号。\n用户：%s",
                userContent
        );

        logger.debug("异步生成会话标题, memoryId={}", memoryId);

        // 通过 LLMService 调用 LLM（带熔断+超时）
        return llmService.chat(prompt)
                .thenApply(title -> {
                    if (title == null || title.isBlank()) {
                        return "新对话";
                    }
                    return title.replace("\"", "").replace("标题", "").trim();
                });
    }
}
