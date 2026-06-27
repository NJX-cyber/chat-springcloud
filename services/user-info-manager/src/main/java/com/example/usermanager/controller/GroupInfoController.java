package com.example.usermanager.controller;

import com.example.model.annotation.GlobalInterceptor;
import com.example.model.annotation.RateLimit;
import com.example.model.entity.dto.TokenUserInfoDto;
import com.example.model.entity.po.GroupInfo;
import com.example.model.entity.po.UserContact;
import com.example.model.entity.query.GroupInfoQuery;
import com.example.model.entity.query.UserContactQuery;
import com.example.model.entity.vo.GroupInfoVO;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.enums.GroupStatusEnum;
import com.example.model.enums.MessageTypeEnum;
import com.example.model.enums.UserContactStatusEnum;
import com.example.model.controller.BaseController;
import com.example.model.exception.BusinessException;
import com.example.usermanager.service.GroupInfoService;
import com.example.usermanager.service.UserContactService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
    @RateLimit
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
    @RateLimit
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
    @RateLimit
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
    @RateLimit
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
    @RateLimit
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
    @RateLimit
    @PostMapping("/leaveGroup")
    public ResponseVO quitGroup(HttpServletRequest request,
                                @NotEmpty String groupId) {
        TokenUserInfoDto tokenUserinfo = getTokenUserinfo(request);
        this.groupInfoService.quitGroup(tokenUserinfo.getUserId(), groupId, MessageTypeEnum.LEAVE_GROUP);
        return getSuccessResponseVO(null);
    }

    @GlobalInterceptor
    @RateLimit
    @PutMapping("/dissolutionGroup")
    public ResponseVO dissolutionGroup(HttpServletRequest request,
                                       @NotEmpty String groupId) {
        TokenUserInfoDto tokenUserinfo = getTokenUserinfo(request);
        this.groupInfoService.dissolutionGroup(tokenUserinfo.getUserId(), groupId, GroupStatusEnum.DISSOLUTION.getStatus());
        return getSuccessResponseVO(null);
    }
}