package com.chat.websocket;

import com.chat.entity.dto.MessageSendDto;
import com.chat.utils.JsonUtils;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * author:normal
 * date:2026/1/27 16:25
 * description:
 */
@Component("messageHandle")
public class MessageHandle {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandle.class);

    private static final String MESSAGE_TOPIC = "message.topic";

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @PostConstruct
    public void listenMessage() {
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.addListener(MessageSendDto.class, (MessageSendDto, dto) -> {
            logger.info("收到关播消息{}", JsonUtils.convertObj2Json(dto));
            this.channelContextUtils.sendMessage(dto);
        });
    }

    public void sendMessage(MessageSendDto messageSendDto) {
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.publish(messageSendDto);
    }
}