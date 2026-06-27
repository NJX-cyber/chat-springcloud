package com.example.usermanager.service.impl;

import com.example.model.Redis.RedisComponent;
import com.example.model.config.AppConfig;
import com.example.model.constants.Constants;
import com.example.model.entity.dto.MessageSendDto;
import com.example.model.entity.dto.SystemSettingsDto;
import com.example.model.entity.dto.TokenUserInfoDto;
import com.example.model.entity.po.*;
import com.example.model.entity.query.GroupInfoQuery;
import com.example.model.entity.query.SimplePage;
import com.example.model.entity.query.UserContactQuery;
import com.example.model.entity.query.UserInfoQuery;
import com.example.model.entity.vo.PaginationResultVO;
import com.example.model.enums.*;
import com.example.model.exception.BusinessException;
import com.example.model.feign.ChatMessageFeignClient;
import com.example.model.utils.CopyUtils;
import com.example.model.utils.StringUtils;
import com.example.usermanager.mapper.GroupInfoMapper;
import com.example.usermanager.mapper.UserContactMapper;
import com.example.usermanager.mapper.UserInfoMapper;
import com.example.usermanager.service.GroupInfoService;
import com.example.usermanager.service.UserContactService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @Description:群组ServiceImpl
 * @author:normal
 * @Date:2026/01/06 14:52:55
 */
@Service("groupInfoService")
public class GroupInfoServiceImpl implements GroupInfoService {

    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private UserContactService userContactService;

    @Resource
    private AppConfig appConfig;

    @Resource
    @Lazy
    private GroupInfoService groupInfoService;

    @Resource
    private ChatMessageFeignClient chatMessageFeignClient;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<GroupInfo> findListByParam(GroupInfoQuery query) {
        return this.groupInfoMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(GroupInfoQuery query) {
        return this.groupInfoMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<GroupInfo> findListByPage(GroupInfoQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNum(), count, pageSize);
        query.setSimplePage(page);
        List<GroupInfo> list = this.findListByParam(query);
        PaginationResultVO<GroupInfo> paginationResultVO = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNum(), page.getPageTotal(), list);
        return paginationResultVO;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(GroupInfo bean) {
        return this.groupInfoMapper.insert(bean);
    }

    /**
     * 新增或修改
     */
    @Override
    public Integer addOrUpdate(GroupInfo bean) {
        return this.groupInfoMapper.insertOrUpdate(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<GroupInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.groupInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    @Override
    public Integer addOrUpdateBatch(List<GroupInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.groupInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据GroupId查询
     */
    @Override
    public GroupInfo getGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.selectByGroupId(groupId);
    }

    /**
     * 根据GroupId更新
     */
    @Override
    public Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId) {
        return this.groupInfoMapper.updateByGroupId(bean, groupId);
    }

    /**
     * 根据GroupId删除
     */
    @Override
    public Integer deleteGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.deleteByGroupId(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
        // 新增
        Date date = new Date();
        if (StringUtils.isEmpty(groupInfo.getGroupId())) {
            GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
            groupInfoQuery.setGroupOwnerId(groupInfo.getGroupOwnerId());
            groupInfoQuery.setStatus(GroupStatusEnum.NORMAL.getStatus());
            Integer count = this.groupInfoMapper.selectCount(groupInfoQuery);
            SystemSettingsDto systemSettingsDto = redisComponent.getSystemSettings();
            if (count >= systemSettingsDto.getMaxGroupCount()) {
                throw new BusinessException("最多支持创建" + systemSettingsDto.getMaxGroupCount() + "个群组");
            }

            if (avatarFile == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }

            groupInfo.setCreateTime(date);
            groupInfo.setGroupId(StringUtils.getGroupId());
            groupInfo.setStatus(GroupStatusEnum.NORMAL.getStatus());
            this.groupInfoMapper.insert(groupInfo);

            // 将群组添加为联系人
            UserContact userContact = new UserContact();
            userContact.setCreateTime(date);
            userContact.setLastUpdateTime(date);
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setUserId(groupInfo.getGroupOwnerId());
            userContact.setContactType(UserContactTypeEnum.GROUP.getType());
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            this.userContactMapper.insert(userContact);
            String sessionId = StringUtils.getChatSessionId4Group(groupInfo.getGroupId());
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSession.setLastReceiveTime(date.getTime());
            this.chatMessageFeignClient.insertChatSession(chatSession);
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setUserId(groupInfo.getGroupOwnerId());
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUser.setSessionId(sessionId);
            chatSessionUser.setContactId(groupInfo.getGroupId());
            this.chatMessageFeignClient.insertChatSessionUser(chatSessionUser);
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
            chatMessage.setMessageType(MessageTypeEnum.GROUP_CREATE.getType());
            chatMessage.setContactId(groupInfo.getGroupId());
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
            chatMessage.setMessageContent(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatMessage.setSendTime(date.getTime());
            this.chatMessageFeignClient.insertOrUpdateMessage(chatMessage);
            // 将群组添加至联系人
            redisComponent.saveUserContact(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());

            // 将联系人添加至通道
            this.chatMessageFeignClient.addUser2Group(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());

            // 发送ws消息
            chatSessionUser.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSessionUser.setLastReceiveTime(date.getTime());
            chatSessionUser.setMemberCount(1);
            MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);
            messageSendDto.setExtendData(chatSessionUser);
            messageSendDto.setLastMessage(chatSessionUser.getLastMessage());
            this.chatMessageFeignClient.sendMessage(messageSendDto);
        } else {
            GroupInfo info = this.groupInfoMapper.selectByGroupId(groupInfo.getGroupId());
            if (!info.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            this.groupInfoMapper.updateByGroupId(groupInfo, groupInfo.getGroupId());

            // 更新相关表的冗余信息
            String contactNameUpdate = groupInfo.getGroupName();
            if (!info.getGroupName().equals(groupInfo.getGroupName())) {
                contactNameUpdate = groupInfo.getGroupName();
            }

            // 发送WS消息
            this.chatMessageFeignClient.updateRedundancyInfo(contactNameUpdate, groupInfo.getGroupId());

        }

        if (null == avatarFile) {
            return;
        }
        String projectFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
        File file = new File(projectFolder + Constants.FILE_FOLDER_AVATAR);
        if (!file.exists()) {
            file.mkdirs();
        }
        String filePath = file.getPath() + "/" + groupInfo.getGroupId();
        avatarFile.transferTo(new File(filePath + Constants.IMG_SUFFIX));
        avatarCover.transferTo(new File(filePath + Constants.COVER_IMG_SUFFIX));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dissolutionGroup(String groupOwnerId, String groupId, Integer status) {
        GroupInfo groupInfo = this.groupInfoMapper.selectByGroupId(groupId);
        if (groupInfo == null || !groupInfo.getGroupOwnerId().equals(groupOwnerId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        // 删除群组
        groupInfo = new GroupInfo();
        groupInfo.setStatus(GroupStatusEnum.DISSOLUTION.getStatus());
        this.groupInfoMapper.updateByGroupId(groupInfo, groupId);

        // 移除群成员
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        userContactQuery.setStatus(UserContactTypeEnum.GROUP.getType());

        UserContact userContact = new UserContact();
        userContact.setStatus(UserContactStatusEnum.DEL.getStatus());
        this.userContactMapper.updateByParam(userContact, userContactQuery);

        List<UserContact> contactList = this.userContactMapper.selectList(userContactQuery);
        for (UserContact contact : contactList) {
            this.redisComponent.removeUserContact(contact.getUserId(), contact.getContactId());
        }

        String sessionId = StringUtils.getChatSessionId4Group(groupId);
        Date currentDate = new Date();
        String message = MessageTypeEnum.DISSOLUTION_GROUP.getInitMessage();

        ChatSession chatSession = new ChatSession();
        chatSession.setLastMessage(message);
        chatSession.setSessionId(sessionId);
        chatSession.setLastReceiveTime(currentDate.getTime());
        this.chatMessageFeignClient.updateBySessionId(chatSession, sessionId);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setSendTime(currentDate.getTime());
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
        chatMessage.setMessageType(MessageTypeEnum.DISSOLUTION_GROUP.getType());
        chatMessage.setContactId(groupId);
        chatMessage.setMessageContent(message);
        this.chatMessageFeignClient.insertOrUpdateMessage(chatMessage);
        MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);
        this.chatMessageFeignClient.sendMessage(messageSendDto);
    }


    @Override
    public void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto, String groupId, String selectContacts, Integer opType) {
        GroupInfo groupInfo = this.groupInfoMapper.selectByGroupId(groupId);
        if (null == groupInfo || !groupInfo.getGroupOwnerId().equals(tokenUserInfoDto.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String[] contactList = selectContacts.split(",");
        for (String contactId : contactList) {
            if (OptionTypeEnum.ZERO.getType().equals(opType)) {
                this.groupInfoService.quitGroup(contactId,groupId,MessageTypeEnum.REMOVE_GROUP);
            } else {
                this.userContactService.addContact(contactId, groupInfo.getGroupId(), groupId, UserContactTypeEnum.GROUP.getType(), null);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quitGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum) {
        GroupInfo groupInfo = this.groupInfoMapper.selectByGroupId(groupId);
        if (groupInfo == null || groupInfo.getGroupOwnerId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        Integer count = this.userContactMapper.deleteByUserIdAndContactId(userId, groupId);
        if (count == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserInfo userInfo = this.userInfoMapper.selectByUserId(userId);
        String sessionId = StringUtils.getChatSessionId4Group(groupId);
        Date date = new Date();
        String message = String.format(messageTypeEnum.getInitMessage(), userInfo.getNickname());

        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(message);
        chatSession.setLastReceiveTime(date.getTime());
        this.chatMessageFeignClient.updateBySessionId(chatSession, sessionId);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageContent(message);
        chatMessage.setContactId(groupId);
        chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
        chatMessage.setMessageType(messageTypeEnum.getType());
        chatMessage.setSendTime(date.getTime());
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        this.chatMessageFeignClient.insertOrUpdateMessage(chatMessage);

        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        Integer memberCount = this.userContactMapper.selectCount(userContactQuery);
        MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);
        messageSendDto.setExtendData(userId);
        messageSendDto.setMemberCount(memberCount);
        this.chatMessageFeignClient.sendMessage(messageSendDto);
    }
}