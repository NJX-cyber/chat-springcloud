package com.chat.service.impl;

import com.chat.entity.dto.MessageSendDto;
import com.chat.entity.po.ChatSessionUser;
import com.chat.entity.po.UserContact;
import com.chat.entity.query.SimplePage;
import com.chat.entity.query.ChatSessionUserQuery;
import com.chat.entity.query.UserContactQuery;
import com.chat.entity.vo.PaginationResultVO;
import com.chat.enums.MessageTypeEnum;
import com.chat.enums.PageSize;
import com.chat.enums.UserContactStatusEnum;
import com.chat.enums.UserContactTypeEnum;
import com.chat.mapper.UserContactMapper;
import com.chat.websocket.MessageHandle;
import org.springframework.stereotype.Service;
import com.chat.service.ChatSessionUserService;
import com.chat.mapper.ChatSessionUserMapper;

import javax.annotation.Resource;
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
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private MessageHandle messageHandle;

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
            List<UserContact> contactList = this.userContactMapper.selectList(userContactQuery);
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