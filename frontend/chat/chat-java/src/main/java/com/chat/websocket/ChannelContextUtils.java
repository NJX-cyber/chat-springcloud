package com.chat.websocket;

import com.chat.Redis.RedisComponent;
import com.chat.constants.Constants;
import com.chat.entity.dto.MessageSendDto;
import com.chat.entity.dto.WsInitData;
import com.chat.entity.po.ChatMessage;
import com.chat.entity.po.ChatSessionUser;
import com.chat.entity.po.UserInfo;
import com.chat.entity.query.ChatMessageQuery;
import com.chat.entity.query.ChatSessionUserQuery;
import com.chat.entity.query.UserContactApplyQuery;
import com.chat.enums.MessageTypeEnum;
import com.chat.enums.UserContactApplyStatusEnum;
import com.chat.enums.UserContactTypeEnum;
import com.chat.mapper.ChatSessionUserMapper;
import com.chat.service.ChatMessageService;
import com.chat.service.UserContactApplyService;
import com.chat.service.UserInfoService;
import com.chat.utils.JsonUtils;
import com.chat.utils.StringUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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

    private static final ConcurrentHashMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private UserContactApplyService applyService;

    public void addContext(String userId, Channel channel) {
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

        // 更新最后登录时间
        UserInfo userInfo = new UserInfo();
        userInfo.setRecentLoginTime(new Date());
        this.userInfoService.updateUserInfoByUserId(userInfo, userId);

        // 给用户发送消息
        UserInfo dbUserInfo = this.userInfoService.getUserInfoByUserId(userId);
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
        // 查询所有的联系人
        List<String> idList = contactIdList.stream().filter(item -> item.startsWith(UserContactTypeEnum.GROUP.getPrefix())).collect(Collectors.toList());
        idList.add(userId);
        ChatMessageQuery messageQuery = new ChatMessageQuery();
        messageQuery.setContactIdList(contactIdList);
        messageQuery.setLastReceiveTime(userLastOffTime);
        List<ChatMessage> messageList = this.chatMessageService.findListByParam(messageQuery);
        wsInitData.setChatMessageList(messageList);

        /**
         * 3、查询好友申请
         */
        UserContactApplyQuery applyQuery = new UserContactApplyQuery();
        applyQuery.setReceiverUserId(userId);
        applyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
        applyQuery.setLastReceiveTime(userLastOffTime);
        Integer applyCount = this.applyService.findCountByParam(applyQuery);
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
        if (!StringUtils.isEmpty(userId)) {
            USER_CONTEXT_MAP.remove(userId);
        }
        redisComponent.removeUserHeartBeat(userId);
        // 更新最新离线时间
        UserInfo userInfo = new UserInfo();
        userInfo.setRecentOfflineTime(System.currentTimeMillis());
        this.userInfoService.updateUserInfoByUserId(userInfo, userId);
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