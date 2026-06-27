package com.example.model.feign;

import com.example.model.entity.dto.MessageSendDto;
import com.example.model.entity.po.ChatMessage;
import com.example.model.entity.po.ChatSession;
import com.example.model.entity.po.ChatSessionUser;
import com.example.model.entity.query.ChatMessageQuery;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.feign.fallback.ChatMessageFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * chat-message-manager 服务 Feign 客户端
 * author:normal
 * date:2026/6/15
 */
@FeignClient(name = "chat-message-manager", path = "/feign", fallbackFactory = ChatMessageFeignClientFallback.class)
public interface ChatMessageFeignClient {

    /**
     * 强制关闭用户连接（下线）
     */
    @PostMapping("/closeContact/{userId}")
    ResponseVO<Void> closeContact(@PathVariable("userId") String userId);

    /**
     * 查询聊天消息
     */
    @PostMapping("/findListByParam")
    List<ChatMessage> findListByParam(@RequestBody ChatMessageQuery messageQuery);

    /**
     * 插入聊天会话
     */
    @PostMapping("/insertChatSession")
    void insertChatSession(@RequestBody ChatSession chatSession);

    /**
     * 插入聊天会话用户
     */
    @PostMapping("/insertChatSessionUser")
    void insertChatSessionUser(@RequestBody ChatSessionUser chatSessionUser);

    /**
     * 插入或更新聊天消息
     */
    @PostMapping("/insertOrUpdateMessage")
    void insertOrUpdateMessage(@RequestBody ChatMessage chatMessage);

    /**
     * 添加用户到群组
     */
    @GetMapping("/addUser2Group")
    void addUser2Group(@RequestParam String groupOwnerId, @RequestParam String groupId);

    /**
     * 发送消息
     */
    @PostMapping("/sendMessage")
    void sendMessage(@RequestBody MessageSendDto messageSendDto);

    /**
     * 更新冗余信息
     */
    @GetMapping("/updateRedundancyInfo")
    void updateRedundancyInfo(@RequestParam String contactNameUpdate, @RequestParam String groupId);

    /**
     * 更新会话信息
     */
    @PostMapping("/updateBySessionId")
    void updateBySessionId(@RequestBody ChatSession chatSession, @RequestParam String sessionId);

    /**
     * 批量插入或更新会话
     */
    @PostMapping("/insertOrUpdateChatSessionBatch")
    void insertOrUpdateChatSessionUserBatch(@RequestBody List<ChatSessionUser> chatSessionUserList);
}
