package com.example.usermanager.service;

import com.example.model.entity.dto.UserContactSearchResultDto;
import com.example.model.entity.po.UserContact;
import com.example.model.entity.query.UserContactQuery;
import com.example.model.entity.vo.PaginationResultVO;
import com.example.model.enums.UserContactStatusEnum;

import java.util.List;

/**
 * @Description:联系人Service
 * @author:normal
 * @Date:2026/01/06 14:52:55
 */
public interface UserContactService {

    /**
     * 根据条件查询列表
     */
    List<UserContact> findListByParam(UserContactQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(UserContactQuery query);

    /**
     * 分页查询
     */
    PaginationResultVO<UserContact> findListByPage(UserContactQuery query);

    /**
     * 新增
     */
    Integer add(UserContact bean);

    /**
     * 新增或修改
     */
    Integer addOrUpdate(UserContact bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<UserContact> listBean);

    /**
     * 批量新增或修改
     */
    Integer addOrUpdateBatch(List<UserContact> listBean);

    /**
     * 根据UserIdAndContactId查询
     */
    UserContact getUserContactByUserIdAndContactId(String userId, String contactId);

    /**
     * 根据UserIdAndContactId更新
     */
    Integer updateUserContactByUserIdAndContactId(UserContact bean, String userId, String contactId);

    /**
     * 根据UserIdAndContactId删除
     */
    Integer deleteUserContactByUserIdAndContactId(String userId, String contactId);

    UserContactSearchResultDto searchContact(String userId, String contactId);

    void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo);

    void delOrBlacklistUserContact(String userId, String contactId, UserContactStatusEnum statusEnum);

    void addContact4Robot(String userId);
}