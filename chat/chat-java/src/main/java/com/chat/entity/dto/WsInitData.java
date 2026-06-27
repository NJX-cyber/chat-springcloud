package com.chat.entity.dto;

import com.chat.entity.po.ChatMessage;
import com.chat.entity.po.ChatSessionUser;

import java.util.List;

/**
 * author:normal
 * date:2026/1/26 16:29
 * description:
 */
public class WsInitData {
    private List<ChatSessionUser> chatSessionList;

    private List<ChatMessage> chatMessageList;

    private Integer applyCount;

    public WsInitData(List<ChatSessionUser> chatSessionUserList, List<ChatMessage> chatMessageList, Integer applyCount) {
        this.chatSessionList = chatSessionUserList;
        this.chatMessageList = chatMessageList;
        this.applyCount = applyCount;
    }

    public WsInitData() {

    }

    public List<ChatSessionUser> getChatSessionList() {
        return chatSessionList;
    }

    public void setChatSessionList(List<ChatSessionUser> chatSessionList) {
        this.chatSessionList = chatSessionList;
    }

    public List<ChatMessage> getChatMessageList() {
        return chatMessageList;
    }

    public void setChatMessageList(List<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }

    public Integer getApplyCount() {
        return applyCount;
    }

    public void setApplyCount(Integer applyCount) {
        this.applyCount = applyCount;
    }
}