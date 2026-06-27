package com.chat.controller;

import com.chat.annotation.GlobalInterceptor;
import com.chat.entity.dto.TokenUserInfoDto;
import com.chat.entity.po.GroupInfo;
import com.chat.entity.po.UserContact;
import com.chat.entity.query.GroupInfoQuery;
import com.chat.entity.query.UserContactQuery;
import com.chat.entity.vo.GroupInfoVO;
import com.chat.entity.vo.ResponseVO;
import com.chat.enums.GroupStatusEnum;
import com.chat.enums.MessageTypeEnum;
import com.chat.enums.UserContactStatusEnum;
import com.chat.exception.BusinessException;
import com.chat.service.GroupInfoService;
import com.chat.service.UserContactService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 * @Description:群组Controller
 * @author:normal
 * @Date:2026/01/06 14:59:46
 */
@RestController("groupInfoController")
@RequestMapping("/groupInfo")
@Validated
public class GroupInfoController extends BaseController {

    @Resource
    private GroupInfoService groupInfoService;

    @Resource
    private UserContactService userContactService;

    /**
     * 根据条件查询列表
     */
    @GlobalInterceptor
    @PostMapping("/saveGroup")
    public ResponseVO saveGroup(HttpServletRequest request, String groupId,
                                @NotEmpty String groupName,
                                String groupNotice,
                                @NotNull Integer joinType,
                                MultipartFile avatarFile,
                                MultipartFile avatarCover) throws IOException {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserinfo(request);

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupNotice(groupNotice);
        groupInfo.setGroupOwnerId(tokenUserInfoDto.getUserId());
        groupInfo.setJoinType(joinType);

        this.groupInfoService.saveGroup(groupInfo, avatarFile, avatarCover);

        return getSuccessResponseVO(null);
    }

    @GlobalInterceptor
    @GetMapping("/loadMyGroup")
    public ResponseVO loadMyGroup(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserinfo(request);
        GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
        groupInfoQuery.setGroupOwnerId(tokenUserInfoDto.getUserId());
        groupInfoQuery.setStatus(GroupStatusEnum.NORMAL.getStatus());
        groupInfoQuery.setOrderBy("create_time desc");
        List<GroupInfo> list = this.groupInfoService.findListByParam(groupInfoQuery);
        return getSuccessResponseVO(list);
    }

    @GlobalInterceptor
    @GetMapping("/getGroupInfo")
    public ResponseVO getGroupInfo(HttpServletRequest request, @NotEmpty String groupId) {
        GroupInfo groupInfo = getGroupDetail(request, groupId);
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        Integer count = this.userContactService.findCountByParam(userContactQuery);
        groupInfo.setMemberCount(count);
        return getSuccessResponseVO(groupInfo);
    }

    @GlobalInterceptor
    @GetMapping("/getGroupInfo4Chat")
    public ResponseVO getGroupInfoForChat(HttpServletRequest request, @NotEmpty String groupId) {
        GroupInfo groupInfo = getGroupDetail(request, groupId);
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        userContactQuery.setQueryUserInfo(true);
        userContactQuery.setOrderBy("create_time asc");
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> userContactList = this.userContactService.findListByParam(userContactQuery);

        GroupInfoVO groupInfoVO = new GroupInfoVO();
        groupInfoVO.setGroupInfo(groupInfo);
        groupInfoVO.setUserContactList(userContactList);
        return getSuccessResponseVO(groupInfoVO);
    }

    public GroupInfo getGroupDetail(HttpServletRequest request, String groupId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserinfo(request);
        UserContact userContact = this.userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(), groupId);
        if (userContact == null || !userContact.getStatus().equals(UserContactStatusEnum.FRIEND.getStatus())) {
            throw new BusinessException("你不在群聊中或者群聊不存在!");
        }

        GroupInfo groupInfo = this.groupInfoService.getGroupInfoByGroupId(groupId);
        if (groupInfo == null || GroupStatusEnum.DISSOLUTION.getStatus().equals(groupInfo.getStatus())) {
            throw new BusinessException("群聊不存在或已解散!");
        }
        return groupInfo;
    }

    @GlobalInterceptor
    @PostMapping("/addOrRemoveGroupMember")
    public ResponseVO addOrRemoveGroupUser(HttpServletRequest request,
                                           @NotEmpty String groupId,
                                           @NotEmpty String selectContacts,
                                           @NotNull Integer opType) {
        TokenUserInfoDto tokenUserinfo = getTokenUserinfo(request);
        this.groupInfoService.addOrRemoveGroupUser(tokenUserinfo, groupId, selectContacts, opType);
        return getSuccessResponseVO(null);
    }

    @GlobalInterceptor
    @PostMapping("/leaveGroup")
    public ResponseVO quitGroup(HttpServletRequest request,
                                @NotEmpty String groupId) {
        TokenUserInfoDto tokenUserinfo = getTokenUserinfo(request);
        this.groupInfoService.quitGroup(tokenUserinfo.getUserId(), groupId, MessageTypeEnum.LEAVE_GROUP);
        return getSuccessResponseVO(null);
    }

    @GlobalInterceptor
    @PutMapping("/dissolutionGroup")
    public ResponseVO dissolutionGroup(HttpServletRequest request,
                                       @NotEmpty String groupId) {
        TokenUserInfoDto tokenUserinfo = getTokenUserinfo(request);
        this.groupInfoService.dissolutionGroup(tokenUserinfo.getUserId(), groupId, GroupStatusEnum.DISSOLUTION.getStatus());
        return getSuccessResponseVO(null);
    }
}