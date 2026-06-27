package com.example.usermanager.service.impl;

import com.example.model.Redis.RedisComponent;
import com.example.model.entity.dto.MessageSendDto;
import com.example.model.entity.dto.SystemSettingsDto;
import com.example.model.entity.dto.UserContactSearchResultDto;
import com.example.model.entity.po.*;
import com.example.model.entity.query.*;
import com.example.model.entity.vo.PaginationResultVO;
import com.example.model.enums.*;
import com.example.model.exception.BusinessException;
import com.example.model.feign.ChatMessageFeignClient;
import com.example.model.utils.CopyUtils;
import com.example.model.utils.StringUtils;
import com.example.usermanager.mapper.GroupInfoMapper;
import com.example.usermanager.mapper.UserContactMapper;
import com.example.usermanager.mapper.UserInfoMapper;
import com.example.usermanager.service.UserContactService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description:联系人ServiceImpl
 * @author:normal
 * @Date:2026/01/06 14:52:55
 */
@Service("userContactService")
public class UserContactServiceImpl implements UserContactService {

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;


    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ChatMessageFeignClient chatMessageFeignClient;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserContact> findListByParam(UserContactQuery query) {
        return this.userContactMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(UserContactQuery query) {
        return this.userContactMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<UserContact> findListByPage(UserContactQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNum(), count, pageSize);
        query.setSimplePage(page);
        List<UserContact> list = this.findListByParam(query);
        PaginationResultVO<UserContact> paginationResultVO = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNum(), page.getPageTotal(), list);
        return paginationResultVO;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserContact bean) {
        return this.userContactMapper.insert(bean);
    }

    /**
     * 新增或修改
     */
    @Override
    public Integer addOrUpdate(UserContact bean) {
        return this.userContactMapper.insertOrUpdate(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserContact> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserContact> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据UserIdAndContactId查询
     */
    @Override
    public UserContact getUserContactByUserIdAndContactId(String userId, String contactId) {
        return this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
    }

    /**
     * 根据UserIdAndContactId更新
     */
    @Override
    public Integer updateUserContactByUserIdAndContactId(UserContact bean, String userId, String contactId) {
        return this.userContactMapper.updateByUserIdAndContactId(bean, userId, contactId);
    }

    /**
     * 根据UserIdAndContactId删除
     */
    @Override
    public Integer deleteUserContactByUserIdAndContactId(String userId, String contactId) {
        return this.userContactMapper.deleteByUserIdAndContactId(userId, contactId);
    }

    @Override
    public UserContactSearchResultDto searchContact(String userId, String contactId) {
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);

        if (typeEnum == null) {
            throw new BusinessException("查询对象不存在!");
        }
        UserContactSearchResultDto resultDto = new UserContactSearchResultDto();
        switch (typeEnum) {
            case USER:
                UserInfo userInfo = this.userInfoMapper.selectByUserId(contactId);
                if (userInfo == null) {
                    throw new BusinessException("查询对象不存在!");
                }
                resultDto = CopyUtils.copy(userInfo, UserContactSearchResultDto.class);
                break;
            case GROUP:
                GroupInfo groupInfo = this.groupInfoMapper.selectByGroupId(contactId);
                if (groupInfo == null) {
                    throw new BusinessException("查询对象不存在!");
                }
                resultDto.setNickname(groupInfo.getGroupName());
                break;
            default:
                break;
        }
        resultDto.setContactType(typeEnum.toString());
        resultDto.setContactId(contactId);

        if (userId.equals(contactId)) {
            resultDto.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return resultDto;
        }

        // 查询是否是好友
        UserContact userContact = this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
        resultDto.setStatus(userContact == null ? null : userContact.getStatus());
        return resultDto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) {
        if (UserContactTypeEnum.GROUP.getType().equals(contactType)) {
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setUserId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer count = this.userContactMapper.selectCount(userContactQuery);
            if (count >= this.redisComponent.getSystemSettings().getMaxGroupCount()) {
                throw new BusinessException("群内人数已达上限，无法加群");
            }
        }

        List<UserContact> list = new ArrayList<>();

        Date currentDate = new Date();
        UserContact userContact = new UserContact();
        userContact.setUserId(applyUserId);
        userContact.setContactId(contactId);
        userContact.setContactType(contactType);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        userContact.setCreateTime(currentDate);
        userContact.setLastUpdateTime(currentDate);
        list.add(userContact);

        // 申请人是好友，则双方均许添加
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            userContact = new UserContact();
            userContact.setUserId(contactId);
            userContact.setContactId(applyUserId);
            userContact.setContactType(contactType);
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContact.setCreateTime(currentDate);
            userContact.setLastUpdateTime(currentDate);
            list.add(userContact);
        }
        // 批量插入
        this.userContactMapper.insertOrUpdateBatch(list);
        this.redisComponent.saveUserContact(applyUserId, receiveUserId);
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            this.redisComponent.saveUserContact(receiveUserId, applyUserId);
        }

        String sessionId = null;
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            sessionId = StringUtils.getChatSessionId4User(new String[]{applyUserId, receiveUserId});
        } else {
            sessionId = StringUtils.getChatSessionId4Group(contactId);
        }

        List<ChatSessionUser> chatSessionUserList = new ArrayList<>();
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            // 创建会话
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(applyInfo);
            chatSession.setLastReceiveTime(currentDate.getTime());
            this.chatMessageFeignClient.insertChatSession(chatSession);

            // 申请人Session
            ChatSessionUser applySessionUser = new ChatSessionUser();
            applySessionUser.setUserId(applyUserId);
            applySessionUser.setContactId(contactId);
            applySessionUser.setSessionId(sessionId);

            UserInfo userInfoReceive = this.userInfoMapper.selectByUserId(contactId);
            applySessionUser.setContactName(userInfoReceive.getNickname());
            chatSessionUserList.add(applySessionUser);

            // 接收人Session
            ChatSessionUser applyReceiveUser = new ChatSessionUser();
            applyReceiveUser.setUserId(contactId);
            applyReceiveUser.setContactId(applyUserId);
            applyReceiveUser.setSessionId(sessionId);
            UserInfo userInfoApply = this.userInfoMapper.selectByUserId(applyUserId);
            applyReceiveUser.setContactName(userInfoApply.getNickname());
            chatSessionUserList.add(applyReceiveUser);
            this.chatMessageFeignClient.insertOrUpdateChatSessionUserBatch(chatSessionUserList);

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageContent(applyInfo);
            chatMessage.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
            chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
            chatMessage.setContactId(contactId);
            chatMessage.setSendUserId(applyUserId);
            chatMessage.setSendUserNickName(applySessionUser.getContactName());
            chatMessage.setSendTime(currentDate.getTime());
            chatMessage.setContactType(UserContactTypeEnum.USER.getType());
            this.chatMessageFeignClient.insertOrUpdateMessage(chatMessage);

            MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);
            // 发送给接收好友申请的人
            this.chatMessageFeignClient.sendMessage(messageSendDto);

            // 发送给申请人
            messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND_SELF.getType());
            messageSendDto.setContactId(applyUserId);
            messageSendDto.setExtendData(userInfoReceive);
            this.chatMessageFeignClient.sendMessage(messageSendDto);
        } else {
            // 加入群组
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setUserId(applyUserId);
            GroupInfo groupInfo = this.groupInfoMapper.selectByGroupId(contactId);
            chatSessionUser.setContactId(groupInfo.getGroupId());
            chatSessionUser.setSessionId(sessionId);
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUser.setContactType(UserContactTypeEnum.GROUP.getType());
            this.chatMessageFeignClient.insertChatSessionUser(chatSessionUser);

            // 将群组添加至联系人
            redisComponent.saveUserContact(applyUserId, groupInfo.getGroupId());

            this.chatMessageFeignClient.addUser2Group(applyUserId, groupInfo.getGroupId());

            UserInfo applyUserInfo = this.userInfoMapper.selectByUserId(applyUserId);
            String message = String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(), applyUserInfo.getNickname());

            // 增加session信息
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastReceiveTime(currentDate.getTime());
            chatSession.setLastMessage(message);
            this.chatMessageFeignClient.insertChatSession(chatSession);

            // 增加聊天消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageContent(message);
            chatMessage.setMessageType(MessageTypeEnum.ADD_GROUP.getType());
            chatMessage.setSendTime(currentDate.getTime());
            chatMessage.setContactId(contactId);
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
            chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
            this.chatMessageFeignClient.insertOrUpdateMessage(chatMessage);

            // 将群组添加为联系人
            redisComponent.saveUserContact(applyUserId, groupInfo.getGroupId());

            //将联系人通道添加至群组通道
            this.chatMessageFeignClient.addUser2Group(applyUserId, groupInfo.getGroupId());

            // 发送群消息
            MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);
            messageSendDto.setExtendData(contactId);
            messageSendDto.setContactName(groupInfo.getGroupName());

            // 获取群成员数量
            UserContactQuery contactQuery = new UserContactQuery();
            contactQuery.setContactId(contactId);
            contactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer count = this.userContactMapper.selectCount(contactQuery);

            messageSendDto.setMemberCount(count);
            this.chatMessageFeignClient.sendMessage(messageSendDto);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delOrBlacklistUserContact(String userId, String contactId, UserContactStatusEnum statusEnum) {
        // 更新自己的联系人状态
        UserContact userContact = new UserContact();
        userContact.setStatus(statusEnum.getStatus());
        this.userContactMapper.updateByUserIdAndContactId(userContact, userId, contactId);

        // 更新联系人中自己的状态
        UserContact friendContact = new UserContact();
        if (statusEnum.equals(UserContactStatusEnum.DEL)) {
            friendContact.setStatus(UserContactStatusEnum.DEL_BY.getStatus());
        } else if (statusEnum.equals(UserContactStatusEnum.BLACKLIST)) {
            friendContact.setStatus(UserContactStatusEnum.BLACKLIST_BY.getStatus());
        }
        this.userContactMapper.updateByUserIdAndContactId(friendContact, contactId, userId);

        this.redisComponent.removeUserContact(contactId, userId);
        this.redisComponent.removeUserContact(userId, contactId);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addContact4Robot(String userId) {
        Date currentDate = new Date();
        SystemSettingsDto systemSettings = redisComponent.getSystemSettings();
        String robotUid = systemSettings.getRobotUid();
        String robotWelcome = systemSettings.getRobotWelcome();
        String robotNickName = systemSettings.getRobotNickName();
        robotWelcome = StringUtils.cleanHtmlTag(robotWelcome);
        // 增加机器人好友
        UserContact userContact = new UserContact();
        userContact.setContactId(robotUid);
        userContact.setContactName(robotNickName);
        userContact.setUserId(userId);
        userContact.setContactType(UserContactTypeEnum.USER.getType());
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        userContact.setLastUpdateTime(currentDate);
        userContact.setCreateTime(currentDate);
        this.userContactMapper.insert(userContact);
        // 增加会话信息
        String sessionId = StringUtils.getChatSessionId4User(new String[]{userId, robotUid});
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(robotWelcome);
        chatSession.setLastReceiveTime(currentDate.getTime());
        this.chatMessageFeignClient.insertChatSession(chatSession);
        // 增加会话人信息
        ChatSessionUser chatSessionUser = new ChatSessionUser();
        chatSessionUser.setUserId(userId);
        chatSessionUser.setContactId(robotUid);
        chatSessionUser.setContactName(robotNickName);
        chatSessionUser.setSessionId(sessionId);
        this.chatMessageFeignClient.insertChatSessionUser(chatSessionUser);
        // 增加聊天消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSendUserId(robotUid);
        chatMessage.setContactId(robotUid);
        chatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
        chatMessage.setSendUserNickName(robotNickName);
        chatMessage.setSessionId(sessionId);
        chatMessage.setSendTime(currentDate.getTime());
        chatMessage.setContactType(UserContactTypeEnum.USER.getType());
        chatMessage.setMessageContent(robotWelcome);
        chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
        this.chatMessageFeignClient.insertOrUpdateMessage(chatMessage);
    }

}