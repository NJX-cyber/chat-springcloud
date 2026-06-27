package com.example.usermanager.service.impl;

import com.example.model.constants.Constants;
import com.example.model.entity.dto.MessageSendDto;
import com.example.model.entity.dto.TokenUserInfoDto;
import com.example.model.entity.po.GroupInfo;
import com.example.model.entity.po.UserContact;
import com.example.model.entity.po.UserContactApply;
import com.example.model.entity.po.UserInfo;
import com.example.model.entity.query.*;
import com.example.model.entity.vo.PaginationResultVO;
import com.example.model.enums.*;
import com.example.model.exception.BusinessException;
import com.example.model.feign.ChatMessageFeignClient;
import com.example.model.utils.StringUtils;
import com.example.usermanager.mapper.GroupInfoMapper;
import com.example.usermanager.mapper.UserContactApplyMapper;
import com.example.usermanager.mapper.UserContactMapper;
import com.example.usermanager.mapper.UserInfoMapper;
import com.example.usermanager.service.UserContactApplyService;
import com.example.usermanager.service.UserContactService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Description:申请联系人ServiceImpl
 * @author:normal
 * @Date:2026/01/06 14:52:55
 */
@Service("userContactApplyService")
public class UserContactApplyServiceImpl implements UserContactApplyService {

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private UserContactApplyMapper<UserContactApply, UserContactApplyQuery> userContactApplyMapper;

    @Resource
    private UserContactService userContactService;

    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

    @Resource
    private ChatMessageFeignClient chatMessageFeignClient;


    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserContactApply> findListByParam(UserContactApplyQuery query) {
        return this.userContactApplyMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(UserContactApplyQuery query) {
        return this.userContactApplyMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<UserContactApply> findListByPage(UserContactApplyQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNum(), count, pageSize);
        query.setSimplePage(page);
        List<UserContactApply> list = this.findListByParam(query);
        PaginationResultVO<UserContactApply> paginationResultVO = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNum(), page.getPageTotal(), list);
        return paginationResultVO;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserContactApply bean) {
        return this.userContactApplyMapper.insert(bean);
    }

    /**
     * 新增或修改
     */
    @Override
    public Integer addOrUpdate(UserContactApply bean) {
        return this.userContactApplyMapper.insertOrUpdate(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserContactApply> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactApplyMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserContactApply> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactApplyMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据ApplyId查询
     */
    @Override
    public UserContactApply getUserContactApplyByApplyId(Integer applyId) {
        return this.userContactApplyMapper.selectByApplyId(applyId);
    }

    /**
     * 根据ApplyId更新
     */
    @Override
    public Integer updateUserContactApplyByApplyId(UserContactApply bean, Integer applyId) {
        return this.userContactApplyMapper.updateByApplyId(bean, applyId);
    }

    /**
     * 根据ApplyId删除
     */
    @Override
    public Integer deleteUserContactApplyByApplyId(Integer applyId) {
        return this.userContactApplyMapper.deleteByApplyId(applyId);
    }

    /**
     * 根据ApplyUserIdAndReceiverUserIdAndContactId查询
     */
    @Override
    public UserContactApply getUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId(String applyUserId, String receiverUserId, String contactId) {
        return this.userContactApplyMapper.selectByApplyUserIdAndReceiverUserIdAndContactId(applyUserId, receiverUserId, contactId);
    }

    /**
     * 根据ApplyUserIdAndReceiverUserIdAndContactId更新
     */
    @Override
    public Integer updateUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId(UserContactApply bean, String applyUserId, String receiverUserId, String contactId) {
        return this.userContactApplyMapper.updateByApplyUserIdAndReceiverUserIdAndContactId(bean, applyUserId, receiverUserId, contactId);
    }

    /**
     * 根据ApplyUserIdAndReceiverUserIdAndContactId删除
     */
    @Override
    public Integer deleteUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId(String applyUserId, String receiverUserId, String contactId) {
        return this.userContactApplyMapper.deleteByApplyUserIdAndReceiverUserIdAndContactId(applyUserId, receiverUserId, contactId);
    }

    /**
     * 根据LastApplyTime查询
     */
    @Override
    public UserContactApply getUserContactApplyByLastApplyTime(Long lastApplyTime) {
        return this.userContactApplyMapper.selectByLastApplyTime(lastApplyTime);
    }

    /**
     * 根据LastApplyTime更新
     */
    @Override
    public Integer updateUserContactApplyByLastApplyTime(UserContactApply bean, Long lastApplyTime) {
        return this.userContactApplyMapper.updateByLastApplyTime(bean, lastApplyTime);
    }

    /**
     * 根据LastApplyTime删除
     */
    @Override
    public Integer deleteUserContactApplyByLastApplyTime(Long lastApplyTime) {
        return this.userContactApplyMapper.deleteByLastApplyTime(lastApplyTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dealWithApply(String userId, Integer applyId, Integer status) {
        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        if (statusEnum == null || UserContactApplyStatusEnum.INIT.equals(statusEnum)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        UserContactApply contactApply = this.userContactApplyMapper.selectByApplyId(applyId);
        if (contactApply == null || !userId.equals(contactApply.getReceiverUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        UserContactApply userContactApply = this.userContactApplyMapper.selectByApplyId(applyId);
        userContactApply.setStatus(statusEnum.getStatus());

        UserContactApplyQuery userContactApplyQuery = new UserContactApplyQuery();
        userContactApplyQuery.setApplyId(applyId);
        userContactApplyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
        Integer count = this.userContactApplyMapper.updateByParam(userContactApply, userContactApplyQuery);
        if (count == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (UserContactApplyStatusEnum.PASS.equals(statusEnum)) {
            this.userContactService.addContact(userContactApply.getApplyUserId(), userContactApply.getReceiverUserId(),
                    userContactApply.getContactId(), userContactApply.getContactType(), userContactApply.getApplyInfo());
            return;
        }

        if (UserContactApplyStatusEnum.BLACKLIST.equals(statusEnum)) {
            Date currentDate = new Date();
            UserContact userContact = new UserContact();
            userContact.setUserId(contactApply.getApplyUserId());
            userContact.setContactId(contactApply.getContactId());
            userContact.setContactType(contactApply.getContactType());
            userContact.setCreateTime(currentDate);
            userContact.setLastUpdateTime(currentDate);
            userContact.setStatus(UserContactStatusEnum.BLACKLIST_BY_FIRST.getStatus());
            this.userContactMapper.insertOrUpdate(userContact);
        }
    }

    @Override
    public Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo) {
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);

        if (typeEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 默认的申请信息
        applyInfo = StringUtils.isEmpty(applyInfo) ? String.format(Constants.APPLY_INFO_DEFAULT, tokenUserInfoDto.getNickname()) : applyInfo;
        long currentTimeMillis = System.currentTimeMillis();

        Integer joinType = null;
        String receiveUserId = contactId;
        String applyUserId = tokenUserInfoDto.getUserId();
        UserContact userContact = this.userContactMapper.selectByUserIdAndContactId(contactId, applyUserId);
        if (userContact != null && ArrayUtils.contains(new Integer[]{UserContactStatusEnum.BLACKLIST_BY.getStatus(),
                UserContactStatusEnum.BLACKLIST_BY_FIRST.getStatus()}, userContact.getStatus())) {
            throw new BusinessException("对方将你拉黑，无法添加");
        }

        switch (typeEnum) {
            case GROUP:
                GroupInfo groupInfo = this.groupInfoMapper.selectByGroupId(contactId);
                if (groupInfo == null || GroupStatusEnum.DISSOLUTION.getStatus().equals(groupInfo.getStatus())) {
                    throw new BusinessException("群组不存在或已解散");
                }
                receiveUserId = groupInfo.getGroupOwnerId();
                joinType = groupInfo.getJoinType();
                break;
            case USER:
                UserInfo userInfo = this.userInfoMapper.selectByUserId(contactId);
                if (userInfo == null || UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
                    throw new BusinessException("用户信息不存在");
                }
                receiveUserId = userInfo.getUserId();
                joinType = userInfo.getConnectType();
                break;
        }

        if (ConnectTypeEnum.DIRECT_ADD.getType().equals(joinType)) {
            this.userContactService.addContact(applyUserId, receiveUserId, contactId, typeEnum.getType(), applyInfo);
            return joinType;
        }

        UserContactApply userContactApply = this.userContactApplyMapper.selectByApplyUserIdAndReceiverUserIdAndContactId(applyUserId, receiveUserId, contactId);

        UserContactApply contactApply = new UserContactApply();
        if (userContactApply == null) {
            contactApply.setApplyUserId(applyUserId);
            contactApply.setReceiverUserId(receiveUserId);
            contactApply.setContactType(typeEnum.getType());
            contactApply.setContactId(contactId);
            contactApply.setLastApplyTime(currentTimeMillis);
            contactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            contactApply.setApplyInfo(applyInfo);
            this.userContactApplyMapper.insert(contactApply);
        } else {
            contactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            contactApply.setLastApplyTime(currentTimeMillis);
            contactApply.setApplyInfo(applyInfo);
            this.userContactApplyMapper.updateByApplyUserIdAndReceiverUserIdAndContactId(contactApply, applyUserId, receiveUserId, contactId);
        }

        if (userContactApply == null || !UserContactApplyStatusEnum.INIT.getStatus().equals(userContactApply.getStatus())) {
            //发送ws消息
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_APPLY.getType());
            messageSendDto.setMessageContent(applyInfo);
            messageSendDto.setContactId(receiveUserId);
            this.chatMessageFeignClient.sendMessage(messageSendDto);
        }

        return joinType;
    }
}