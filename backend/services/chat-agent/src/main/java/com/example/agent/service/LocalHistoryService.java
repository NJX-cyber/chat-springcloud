package com.example.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.entity.po.ChatMessage;

import java.util.List;

/**
 * author:normal
 * date:2026/6/6 20:11
 * description:
 */

public interface LocalHistoryService extends IService<ChatMessage> {
    List<ChatMessage> getHistory(String userId, Long beginTime, Long endTime);
}