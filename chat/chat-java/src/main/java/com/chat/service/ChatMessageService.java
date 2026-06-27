package com.chat.service;

import com.chat.entity.dto.MessageSendDto;
import com.chat.entity.dto.TokenUserInfoDto;
import com.chat.entity.po.ChatMessage;
import com.chat.entity.query.ChatMessageQuery;
import com.chat.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * @Description:Service
 * @author:normal
 * @Date:2026/01/26 14:34:11
 */
public interface ChatMessageService {

    /**
     * 根据条件查询列表
     */
    List<ChatMessage> findListByParam(ChatMessageQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(ChatMessageQuery query);

    /**
     * 分页查询
     */
    PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery query);

    /**
     * 新增
     */
    Integer add(ChatMessage bean);

    /**
     * 新增或修改
     */
    Integer addOrUpdate(ChatMessage bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<ChatMessage> listBean);

    /**
     * 批量新增或修改
     */
    Integer addOrUpdateBatch(List<ChatMessage> listBean);

    /**
     * 根据MessageId查询
     */
    ChatMessage getChatMessageByMessageId(Long messageId);

    /**
     * 根据MessageId更新
     */
    Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId);

    /**
     * 根据MessageId删除
     */
    Integer deleteChatMessageByMessageId(String userId, Long messageId);

    MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto);

    void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover);

    File downloadFile(TokenUserInfoDto tokenUserInfoDto, Long fileId, Boolean showCover);
}