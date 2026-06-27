package com.chat.service;

import com.chat.entity.dto.TokenUserInfoDto;
import com.chat.entity.po.GroupInfo;
import com.chat.entity.query.GroupInfoQuery;
import com.chat.entity.vo.PaginationResultVO;
import com.chat.enums.MessageTypeEnum;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @Description:群组Service
 * @author:normal
 * @Date:2026/01/06 14:52:55
 */
public interface GroupInfoService {

    /**
     * 根据条件查询列表
     */
    List<GroupInfo> findListByParam(GroupInfoQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(GroupInfoQuery query);

    /**
     * 分页查询
     */
    PaginationResultVO<GroupInfo> findListByPage(GroupInfoQuery query);

    /**
     * 新增
     */
    Integer add(GroupInfo bean);

    /**
     * 新增或修改
     */
    Integer addOrUpdate(GroupInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<GroupInfo> listBean);

    /**
     * 批量新增或修改
     */
    Integer addOrUpdateBatch(List<GroupInfo> listBean);

    /**
     * 根据GroupId查询
     */
    GroupInfo getGroupInfoByGroupId(String groupId);

    /**
     * 根据GroupId更新
     */
    Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId);

    /**
     * 根据GroupId删除
     */
    Integer deleteGroupInfoByGroupId(String groupId);

    void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException;

    void dissolutionGroup(String groupOwnerId, String groupId, Integer status);

    void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto, String groupId, String selectContacts, Integer opType);

    void quitGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum);
}