package com.example.messagemanager.service.impl;

import com.example.messagemanager.mapper.ChatSessionUserMapper;
import com.example.messagemanager.service.ChatSessionUserService;
import com.example.messagemanager.websocket.MessageHandle;
import com.example.model.entity.dto.MessageSendDto;
import com.example.model.entity.po.ChatSessionUser;
import com.example.model.entity.po.UserContact;
import com.example.model.entity.query.ChatSessionUserQuery;
import com.example.model.entity.query.SimplePage;
import com.example.model.entity.query.UserContactQuery;
import com.example.model.entity.vo.PaginationResultVO;
import com.example.model.enums.MessageTypeEnum;
import com.example.model.enums.PageSize;
import com.example.model.enums.UserContactStatusEnum;
import com.example.model.enums.UserContactTypeEnum;
import com.example.model.feign.UserInfoFeignClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:ServiceImpl
 * @author:normal
 * @Date:2026/01/26 14:34:11
 */
@Service("chatSessionUserService")
public class ChatSessionUserServiceImpl implements ChatSessionUserService {

    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;

    @Resource
    private MessageHandle messageHandle;

    @Resource
    private UserInfoFeignClient userInfoFeignClient;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<ChatSessionUser> findListByParam(ChatSessionUserQuery query) {
        return this.chatSessionUserMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(ChatSessionUserQuery query) {
        return this.chatSessionUserMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<ChatSessionUser> findListByPage(ChatSessionUserQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNum(), count, pageSize);
        query.setSimplePage(page);
        List<ChatSessionUser> list = this.findListByParam(query);
        PaginationResultVO<ChatSessionUser> paginationResultVO = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNum(), page.getPageTotal(), list);
        return paginationResultVO;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ChatSessionUser bean) {
        return this.chatSessionUserMapper.insert(bean);
    }

    /**
     * 新增或修改
     */
    @Override
    public Integer addOrUpdate(ChatSessionUser bean) {
        return this.chatSessionUserMapper.insertOrUpdate(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ChatSessionUser> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatSessionUserMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ChatSessionUser> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatSessionUserMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据UserIdAndContactId查询
     */
    @Override
    public ChatSessionUser getChatSessionUserByUserIdAndContactId(String userId, String contactId) {
        return this.chatSessionUserMapper.selectByUserIdAndContactId(userId, contactId);
    }

    /**
     * 根据UserIdAndContactId更新
     */
    @Override
    public Integer updateChatSessionUserByUserIdAndContactId(ChatSessionUser bean, String userId, String contactId) {
        return this.chatSessionUserMapper.updateByUserIdAndContactId(bean, userId, contactId);
    }

    /**
     * 根据UserIdAndContactId删除
     */
    @Override
    public Integer deleteChatSessionUserByUserIdAndContactId(String userId, String contactId) {
        return this.chatSessionUserMapper.deleteByUserIdAndContactId(userId, contactId);
    }

    @Override
    public void updateRedundancyInfo(String contactName, String contactId) {

        ChatSessionUser sessionUser = new ChatSessionUser();
        sessionUser.setContactName(contactName);
        ChatSessionUserQuery sessionQuery = new ChatSessionUserQuery();
        sessionQuery.setContactId(contactId);
        this.chatSessionUserMapper.updateByParam(sessionUser, sessionQuery);

        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (typeEnum.equals(UserContactTypeEnum.USER)) {
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setContactType(UserContactTypeEnum.USER.getType());
            userContactQuery.setContactId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            List<UserContact> contactList = this.userInfoFeignClient.selectContactList(userContactQuery);
            for (UserContact userContact : contactList) {
                MessageSendDto messageSendDto = new MessageSendDto();
                messageSendDto.setContactType(typeEnum.getType());
                messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
                messageSendDto.setContactId(userContact.getUserId());
                messageSendDto.setSendUserId(contactId);
                messageSendDto.setSendUserNickName(contactName);
                messageSendDto.setExtendData(contactName);
                this.messageHandle.sendMessage(messageSendDto);
            }
        } else {
            // 发送WS消息
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setContactType(typeEnum.getType());
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
            messageSendDto.setContactId(contactId);
            messageSendDto.setExtendData(contactName);
            this.messageHandle.sendMessage(messageSendDto);
        }
    }
}