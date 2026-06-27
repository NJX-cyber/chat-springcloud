package com.example.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agent.mapper.LocalHistoryMapper;
import com.example.agent.service.LocalHistoryService;
import com.example.model.entity.po.ChatMessage;
import com.example.model.feign.UserInfoFeignClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * author:normal
 * date:2026/6/6 20:19
 * description:
 */


@Service
public class LocalHistoryServiceImpl extends ServiceImpl<LocalHistoryMapper, ChatMessage> implements LocalHistoryService {

    @Resource
    private UserInfoFeignClient userInfoFeignClient;

    @Override
    public List<ChatMessage> getHistory(String userId, Long beginTime, Long endTime) {
        if (userId == null || beginTime == null || endTime == null) {
            return null;
        }

        List<String> groupIds = this.userInfoFeignClient.selectGroupIdList(userId);

        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(
                wrapper -> {
                    wrapper.eq(ChatMessage::getSendUserId, userId)
                            .or()
                            .eq(ChatMessage::getContactId, userId);

                    if (groupIds != null && !groupIds.isEmpty()) {
                        wrapper.or().in(ChatMessage::getContactId, groupIds);
                    }
                }
        )
                .eq(ChatMessage::getMessageType, 2)
                .ge(ChatMessage::getSendTime, beginTime)
                .le(ChatMessage::getSendTime, endTime)
                .orderByAsc(ChatMessage::getSendTime);
        return this.list(queryWrapper);
    }
}