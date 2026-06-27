package com.chat.service;

import com.chat.entity.po.ChatSessionUser;
import com.chat.entity.query.ChatSessionUserQuery;
import com.chat.entity.vo.PaginationResultVO;
import com.chat.enums.UserContactTypeEnum;

import java.util.List;

/**
 * @Description:Service
 * @author:normal
 * @Date:2026/01/26 14:34:11
 */
public interface ChatSessionUserService {

    /**
     * 根据条件查询列表
     */
    List<ChatSessionUser> findListByParam(ChatSessionUserQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(ChatSessionUserQuery query);

    /**
     * 分页查询
     */
    PaginationResultVO<ChatSessionUser> findListByPage(ChatSessionUserQuery query);

    /**
     * 新增
     */
    Integer add(ChatSessionUser bean);

    /**
     * 新增或修改
     */
    Integer addOrUpdate(ChatSessionUser bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<ChatSessionUser> listBean);

    /**
     * 批量新增或修改
     */
    Integer addOrUpdateBatch(List<ChatSessionUser> listBean);

    /**
     * 根据UserIdAndContactId查询
     */
    ChatSessionUser getChatSessionUserByUserIdAndContactId(String userId, String contactId);

    /**
     * 根据UserIdAndContactId更新
     */
    Integer updateChatSessionUserByUserIdAndContactId(ChatSessionUser bean, String userId, String contactId);

    /**
     * 根据UserIdAndContactId删除
     */
    Integer deleteChatSessionUserByUserIdAndContactId(String userId, String contactId);

    void updateRedundancyInfo(String contactName, String contactId);
}