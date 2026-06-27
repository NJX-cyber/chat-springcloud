package com.example.agent.tools;

import com.example.agent.service.LocalHistoryService;
import com.example.model.entity.po.ChatMessage;
import com.example.model.entity.po.UserInfo;
import com.example.model.feign.UserInfoFeignClient;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class TodoTaskTools {

    @Resource
    private LocalHistoryService localHistoryService;

    @Resource
    private UserInfoFeignClient userInfoFeignClient;

    @Tool(name = "获取本地聊天记录文本",
            value = "当用户要求【提取待办事项】时，必须调用此工具。传入用户要求的开始时间和结束时间。" +
                    "如果用户只指定了一天，那么开始时间和结束时间都是这天，" +
                    "如果用户未指定时间，请先向用户询问时间范围，再调用此工具。工具会返回该时间段内的所有对话文本。")
    public String getChatHistoryText(@ToolMemoryId String userId,
                                     @P(value = "开始时间，格式：yyyy-MM-dd", required = true) String beginTimeStr,
                                     @P(value = "结束时间，格式：yyyy-MM-dd", required = true) String endTimeStr) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate beginDate = LocalDate.parse(beginTimeStr.trim(), formatter);
        LocalDate endDate = LocalDate.parse(endTimeStr.trim(), formatter);
        if (Math.abs(ChronoUnit.DAYS.between(beginDate, endDate)) > 3) {
            return "时间范围不能超过3天。";
        }
        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
        Long beginTime = beginDate.atStartOfDay(shanghaiZone).toInstant().toEpochMilli();
        Long endTime = endDate.atTime(LocalTime.MAX).atZone(shanghaiZone).toInstant().toEpochMilli();
        List<ChatMessage> list = localHistoryService.getHistory(userId, beginTime, endTime);

        if (list == null || list.isEmpty()) {
            return "该时间段内没有聊天记录。";
        }

        // 收集唯一 sendUserId，批量查用户信息
        Set<String> userIds = new HashSet<>();
        for (ChatMessage msg : list) {
            userIds.add(msg.getSendUserId());
        }
        Map<String, String> nicknameMap = new HashMap<>();
        for (String uid : userIds) {
            try {
                UserInfo userInfo = userInfoFeignClient.getUserInfo(uid).getData();
                if (userInfo != null) {
                    nicknameMap.put(uid, userInfo.getNickname());
                }
            } catch (Exception e) {
                nicknameMap.put(uid, uid);
            }
        }

        StringBuilder answer = new StringBuilder();
        for (ChatMessage chatMessage : list) {
            String sendTime = java.time.Instant.ofEpochMilli(chatMessage.getSendTime())
                    .atZone(shanghaiZone)
                    .toLocalDate()
                    .format(formatter);
            String receiverNickname = nicknameMap.getOrDefault(chatMessage.getContactId(), "");
            answer.append(chatMessage.getSendUserNickName())
                    .append("发给")
                    .append(receiverNickname)
                    .append("的消息：")
                    .append(chatMessage.getMessageContent())
                    .append("，发消息的时间是：")
                    .append(sendTime)
                    .append(".\n");
        }
        return answer.toString();
    }
}