package com.example.agent.service;

import com.example.agent.bean.vo.MessageVO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * author:normal
 * date:2026/5/30 9:16
 * description:
 */
public interface ChatMessageService {

    List<MessageVO> getHistory(String memoryId);

    CompletableFuture<String> getTitle(String memoryId);
}