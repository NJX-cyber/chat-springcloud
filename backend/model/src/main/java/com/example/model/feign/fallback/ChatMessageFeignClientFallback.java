package com.example.model.feign.fallback;

import com.example.model.entity.dto.MessageSendDto;
import com.example.model.entity.po.ChatMessage;
import com.example.model.entity.po.ChatSession;
import com.example.model.entity.po.ChatSessionUser;
import com.example.model.entity.query.ChatMessageQuery;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.enums.ResponseCodeEnum;
import com.example.model.exception.BusinessException;
import com.example.model.feign.ChatMessageFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ChatMessageFeignClient 降级工厂
 * author:normal
 * date:2026/6/15
 */
@Component
public class ChatMessageFeignClientFallback implements FallbackFactory<ChatMessageFeignClient> {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageFeignClientFallback.class);

    @Override
    public ChatMessageFeignClient create(Throwable cause) {
        logger.error("ChatMessageFeignClient 远程调用触发降级: {}", cause.getMessage(), cause);
        return new ChatMessageFeignClient() {
            @Override
            public ResponseVO<Void> closeContact(String userId) {
                logger.warn("closeContact 降级处理, userId={}, 关闭连接操作已跳过", userId);
                ResponseVO<Void> response = new ResponseVO<>();
                response.setStatus("success");
                response.setCode(200);
                response.setInfo("ok");
                return response;
            }

            @Override
            public List<ChatMessage> findListByParam(ChatMessageQuery messageQuery) {
                return null;
            }

            @Override
            public void insertChatSession(ChatSession chatSession) {
                throw new BusinessException(ResponseCodeEnum.CODE_500);
            }

            @Override
            public void insertChatSessionUser(ChatSessionUser chatSessionUser) {
                throw new BusinessException(ResponseCodeEnum.CODE_500);
            }

            @Override
            public void insertOrUpdateMessage(ChatMessage chatMessage) {
                throw new BusinessException(ResponseCodeEnum.CODE_500);
            }

            @Override
            public void addUser2Group(String groupOwnerId, String groupId) {
                throw new BusinessException(ResponseCodeEnum.CODE_500);
            }

            @Override
            public void sendMessage(MessageSendDto messageSendDto) {
                throw new BusinessException(ResponseCodeEnum.CODE_500);
            }

            @Override
            public void updateRedundancyInfo(String contactNameUpdate, String groupId) {
                throw new BusinessException(ResponseCodeEnum.CODE_500);
            }

            @Override
            public void updateBySessionId(ChatSession chatSession, String sessionId) {
                throw new BusinessException(ResponseCodeEnum.CODE_500);
            }

            @Override
            public void insertOrUpdateChatSessionUserBatch(List<ChatSessionUser> chatSessionUserList) {
                throw new BusinessException(ResponseCodeEnum.CODE_500);
            }
        };
    }
}
