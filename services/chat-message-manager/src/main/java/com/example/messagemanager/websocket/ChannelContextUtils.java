package com.example.messagemanager.websocket;

import com.example.messagemanager.mapper.ChatSessionUserMapper;
import com.example.model.Redis.RedisComponent;
import com.example.model.constants.Constants;
import com.example.model.entity.po.ChatMessage;
import com.example.model.entity.query.UserContactApplyQuery;
import com.example.model.enums.UserContactApplyStatusEnum;
import com.example.model.feign.ChatMessageFeignClient;
import com.example.model.feign.UserInfoFeignClient;
import com.example.model.entity.dto.MessageSendDto;
import com.example.model.entity.dto.WsInitData;
import com.example.model.entity.po.ChatSessionUser;
import com.example.model.entity.po.UserInfo;
import com.example.model.entity.query.ChatMessageQuery;
import com.example.model.entity.query.ChatSessionUserQuery;
import com.example.model.enums.MessageTypeEnum;
import com.example.model.enums.UserContactTypeEnum;
import com.example.model.utils.JsonUtils;
import com.example.model.utils.StringUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * author:normal
 * date:2026/1/25 19:52
 * description:
 */
@Component("channelContextUtils")
public class ChannelContextUtils {

    private static final Logger logger = LoggerFactory.getLogger(ChannelContextUtils.class);

    private static final ConcurrentHashMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;

    @Resource
    private UserInfoFeignClient userInfoFeignClient;

    @Resource
    private ChatMessageFeignClient chatMessageFeignClient;

    public void addContext(String userId, Channel channel) {
        // 同一用户重连时，先关闭旧连接避免 Connection reset
        Channel oldChannel = USER_CONTEXT_MAP.get(userId);
        if (oldChannel != null && oldChannel != channel) {
            logger.info("用户{}已有旧连接，关闭旧连接并替换为新连接", userId);
            oldChannel.close();
        }
        String channelId = channel.id().toString();
        AttributeKey<String> attributeKey;
        if (!AttributeKey.exists(channelId)) {
            attributeKey = AttributeKey.newInstance(channelId);
        } else {
            attributeKey = AttributeKey.valueOf(channelId);
        }

        channel.attr(attributeKey).set(userId);

        List<String> contactIdList = this.redisComponent.getUserContactList(userId);
        for (String contactId : contactIdList) {
            if (contactId.startsWith(UserContactTypeEnum.GROUP.getPrefix())) {
                add2Group(contactId, channel);
            }
        }
        contactIdList.add(userId);

        USER_CONTEXT_MAP.put(userId, channel);
        redisComponent.saveUserHeartBeat(userId);
        logger.info("用户{}的WebSocket连接已建立，当前在线人数: {}", userId, USER_CONTEXT_MAP.size());

        UserInfo updateLoginTime = new UserInfo();
        updateLoginTime.setRecentLoginTime(new Date());
        try {
            userInfoFeignClient.updateUserInfo(updateLoginTime, userId);
        } catch (Exception e) {
            logger.warn("更新用户最后登录时间失败, userId={}", userId, e);
        }

        UserInfo dbUserInfo = this.userInfoFeignClient.getUserInfo(userId).getData();
        Long dbLastOffTime = dbUserInfo.getRecentOfflineTime();
        Long userLastOffTime = dbLastOffTime;
        if (dbLastOffTime != null && System.currentTimeMillis() - Constants.MILLIS_SECONDS_3DAY_AGO > dbLastOffTime) {
            userLastOffTime = Constants.MILLIS_SECONDS_3DAY_AGO;
        }

        /**
         * 1、查询会话信息 查询用户所有的会话信息，保证会话信息同步
         */
        ChatSessionUserQuery sessionUserQuery = new ChatSessionUserQuery();
        sessionUserQuery.setUserId(userId);
        sessionUserQuery.setOrderBy("last_receive_time desc");
        List<ChatSessionUser> chatSessionUserList = this.chatSessionUserMapper.selectList(sessionUserQuery);

        WsInitData wsInitData = new WsInitData();
        wsInitData.setChatSessionList(chatSessionUserList);

        /**
         * 2、查询聊天消息
         */
        List<String> idList = contactIdList.stream().filter(item -> item.startsWith(UserContactTypeEnum.GROUP.getPrefix())).collect(Collectors.toList());
        idList.add(userId);
        ChatMessageQuery messageQuery = new ChatMessageQuery();
        messageQuery.setContactIdList(contactIdList);
        messageQuery.setLastReceiveTime(userLastOffTime);
        List<ChatMessage> messageList = this.chatMessageFeignClient.findListByParam(messageQuery);
        wsInitData.setChatMessageList(messageList);

        /**
         * 3、查询好友申请
         */
        UserContactApplyQuery applyQuery = new UserContactApplyQuery();
        applyQuery.setReceiverUserId(userId);
        applyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
        applyQuery.setLastReceiveTime(userLastOffTime);
        Integer applyCount = this.userInfoFeignClient.findCountByParam(applyQuery);
        wsInitData.setApplyCount(applyCount);

        // 发送消息
        MessageSendDto<WsInitData> messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDto.setContactId(userId);
        messageSendDto.setExtendData(wsInitData);
        sendMessage(messageSendDto, userId);
    }

    private void add2Group(String groupId, Channel channel) {
        ChannelGroup group = GROUP_CONCURRENT_HASH_MAP.get(groupId);

        if (group == null) {
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONCURRENT_HASH_MAP.put(groupId, group);
        }
        if (channel == null) {
            return;
        }
        group.add(channel);
    }

    public void addUser2Group(String userId, String groupId) {
        ChannelGroup group = GROUP_CONCURRENT_HASH_MAP.get(groupId);
        if (group == null) {
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONCURRENT_HASH_MAP.put(groupId, group);
        }
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (channel == null) {
            return;
        }
        group.add(channel);
    }

    public void removeContext(Channel channel) {
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        if (StringUtils.isEmpty(userId)) {
            logger.info("未认证的连接断开（无userId），跳过清理");
            return;
        }
        USER_CONTEXT_MAP.remove(userId);
        redisComponent.removeUserHeartBeat(userId);
        logger.info("用户{}的WebSocket连接已断开，当前在线人数: {}", userId, USER_CONTEXT_MAP.size());
        UserInfo updateOfflineTime = new UserInfo();
        updateOfflineTime.setRecentOfflineTime(System.currentTimeMillis());
        try {
            userInfoFeignClient.updateUserInfo(updateOfflineTime, userId);
        } catch (Exception e) {
            logger.warn("更新用户离线时间失败, userId={}", userId, e);
        }
    }

    public void sendMessage(MessageSendDto messageSendDto) {
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(messageSendDto.getContactId());
        switch (contactTypeEnum) {
            case USER:
                send2User(messageSendDto);
                break;
            case GROUP:
                send2Group(messageSendDto);
                break;
            default:
                break;
        }
    }

    public void send2User(MessageSendDto messageSendDto) {
        String contactId = messageSendDto.getContactId();
        if (StringUtils.isEmpty(contactId)) {
            return;
        }
        sendMessage(messageSendDto, contactId);
        // 强制下线
        if (MessageTypeEnum.FORCE_OFF_LINE.getType().equals(messageSendDto.getMessageType())) {
            // 关闭通道
            closeContact(contactId);
        }
    }

    public void closeContact(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return;
        }
        redisComponent.cleanUserTokenByUserId(userId);
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (channel != null) {
            channel.close();
        }
        USER_CONTEXT_MAP.remove(userId);
    }

    public void send2Group(MessageSendDto messageSendDto) {
        String contactId = messageSendDto.getContactId();
        if (StringUtils.isEmpty(contactId)) {
            return;
        }
        ChannelGroup channelGroup = GROUP_CONCURRENT_HASH_MAP.get(contactId);
        if (channelGroup == null) {
            logger.warn("群聊{}无活跃成员，消息{}丢弃", contactId, messageSendDto.getMessageId());
            return;
        }
        channelGroup.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));

        // 移出群聊
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(messageSendDto.getMessageType());
        if (MessageTypeEnum.LEAVE_GROUP.equals(messageTypeEnum) || MessageTypeEnum.REMOVE_GROUP.equals(messageTypeEnum)) {
            String userId = (String) messageSendDto.getExtendData();
            this.redisComponent.removeUserContact(userId, messageSendDto.getContactId());
            Channel channel = USER_CONTEXT_MAP.get(userId);
            if (channel != null) {
                channelGroup.remove(channel);
            }
        }

        if (MessageTypeEnum.DISSOLUTION_GROUP.equals(messageTypeEnum)) {
            GROUP_CONCURRENT_HASH_MAP.remove(messageSendDto.getContactId());
            channelGroup.close();
        }

    }


    public void sendMessage(MessageSendDto messageSendDto, String receiveId) {
        Channel channelUser = USER_CONTEXT_MAP.get(receiveId);
        if (channelUser == null) {
            logger.warn("用户{}不在线，消息{}丢弃", receiveId, messageSendDto.getMessageId());
            return;
        }
        // 相对于客户端而言，联系人就是发送人，好友申请的时候不处理
        if (MessageTypeEnum.ADD_FRIEND_SELF.getType().equals(messageSendDto.getMessageType())) {
            UserInfo userInfo = (UserInfo) messageSendDto.getExtendData();
            messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
            messageSendDto.setContactId(userInfo.getUserId());
            messageSendDto.setContactName(userInfo.getNickname());
            messageSendDto.setSendUserNickName(userInfo.getNickname());
            messageSendDto.setExtendData(null);
        } else {
            messageSendDto.setContactId(messageSendDto.getSendUserId());
            messageSendDto.setContactName(messageSendDto.getSendUserNickName());
        }
        channelUser.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));
    }
}