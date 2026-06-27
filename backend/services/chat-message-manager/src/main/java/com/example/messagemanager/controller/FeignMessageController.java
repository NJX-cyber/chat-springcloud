package com.example.messagemanager.controller;

import com.example.messagemanager.service.ChatMessageService;
import com.example.messagemanager.service.ChatSessionService;
import com.example.messagemanager.service.ChatSessionUserService;
import com.example.messagemanager.websocket.ChannelContextUtils;
import com.example.messagemanager.websocket.MessageHandle;
import com.example.model.entity.dto.MessageSendDto;
import com.example.model.entity.po.ChatMessage;
import com.example.model.entity.po.ChatSession;
import com.example.model.entity.po.ChatSessionUser;
import com.example.model.entity.query.ChatMessageQuery;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.controller.BaseController;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供内部 Feign 调用的消息管理端点
 * author:normal
 * date:2026/6/15
 */
@RestController
@RequestMapping("/feign")
public class FeignMessageController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(FeignMessageController.class);

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private MessageHandle messageHandle;

    /**
     * 强制关闭用户连接（内部调用，无需token）
     */
    @PostMapping("/closeContact/{userId}")
    public ResponseVO<Void> closeContact(@PathVariable String userId) {
        logger.debug("Feign 调用: closeContact, userId={}", userId);
        channelContextUtils.closeContact(userId);
        return getSuccessResponseVO(null);
    }

    /**
     * 查询聊天消息列表
     */
    @PostMapping("/findListByParam")
    List<ChatMessage> findListByParam(@RequestBody ChatMessageQuery messageQuery) {
        return chatMessageService.findListByParam(messageQuery);
    }

    /**
     * 插入聊天会话
     */
    @PostMapping("/insertChatSession")
    void insertChatSession(@RequestBody ChatSession chatSession) {
        this.chatSessionService.addOrUpdate(chatSession);
    }

    /**
     * 批量插入或更新会话
     */
    @PostMapping("/insertOrUpdateChatSessionBatch")
    void insertOrUpdateChatSessionBatch(@RequestBody List<ChatSessionUser> chatSessionUserList){
        this.chatSessionUserService.addOrUpdateBatch(chatSessionUserList);
    }

    /**
     * 插入聊天会话用户
     */
    @PostMapping("/insertChatSessionUser")
    void insertChatSessionUser(@RequestBody ChatSessionUser chatSessionUser) {
        this.chatSessionUserService.addOrUpdate(chatSessionUser);
    }

    /**
     * 插入或更新聊天消息
     */
    @PostMapping("/insertOrUpdateMessage")
    void insertOrUpdateMessage(@RequestBody ChatMessage chatMessage) {
        this.chatMessageService.addOrUpdate(chatMessage);
    }

    /**
     * 添加用户到群组
     */
    @GetMapping("/addUser2Group")
    void addUser2Group(@RequestParam String groupOwnerId, @RequestParam String groupId){
        this.channelContextUtils.addUser2Group(groupOwnerId, groupId);
    }

    /**
     * 发送消息
     */
    @PostMapping("/sendMessage")
    void sendMessage(@RequestBody MessageSendDto messageSendDto){
        this.messageHandle.sendMessage(messageSendDto);
    }

    /**
     * 更新冗余信息
     */
    @GetMapping("/updateRedundancyInfo")
    void updateRedundancyInfo(@RequestParam String contactNameUpdate, @RequestParam String groupId){
        this.chatSessionUserService.updateRedundancyInfo(contactNameUpdate, groupId);
    }

    /**
     * 更新会话信息
     */
    @PostMapping("/updateBySessionId")
    void updateBySessionId(@RequestBody ChatSession chatSession, @RequestParam String sessionId){
        this.chatSessionService.updateChatSessionBySessionId(chatSession, sessionId);
    }
}
