package com.example.usermanager.controller;

import com.example.model.annotation.GlobalInterceptor;
import com.example.model.annotation.RateLimit;
import com.example.model.entity.dto.TokenUserInfoDto;
import com.example.model.entity.dto.UserContactSearchResultDto;
import com.example.model.entity.po.GroupInfo;
import com.example.model.entity.po.UserContact;
import com.example.model.entity.po.UserInfo;
import com.example.model.entity.query.UserContactApplyQuery;
import com.example.model.entity.query.UserContactQuery;
import com.example.model.entity.vo.ContactInfoVO;
import com.example.model.entity.vo.PaginationResultVO;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.enums.PageSize;
import com.example.model.enums.ResponseCodeEnum;
import com.example.model.enums.UserContactStatusEnum;
import com.example.model.enums.UserContactTypeEnum;
import com.example.model.controller.BaseController;
import com.example.model.exception.BusinessException;
import com.example.model.utils.CopyUtils;
import com.example.usermanager.service.GroupInfoService;
import com.example.usermanager.service.UserContactApplyService;
import com.example.usermanager.service.UserContactService;
import com.example.usermanager.service.UserInfoService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jodd.util.ArraysUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description:联系人Controller
 * @author:normal
 * @Date:2026/01/06 14:59:46
 */
@RestController("userContactController")
@RequestMapping("/userContact")
@Validated
public class UserContactController extends BaseController {

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private GroupInfoService groupInfoService;

    @Resource
    private UserContactApplyService userContactApplyService;


    @GlobalInterceptor
    @RateLimit
    @GetMapping("/search")
    public ResponseVO search(HttpServletRequest request,
                             @NotEmpty String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserinfo(request);
        UserContactSearchResultDto searchResultDto = this.userContactService.searchContact(tokenUserInfoDto.getUserId(), contactId);
        return getSuccessResponseVO(searchResultDto);
    }

    @GlobalInterceptor
    @RateLimit
    @PostMapping("/applyAdd")
    public ResponseVO applyAdd(HttpServletRequest request,
                               @NotEmpty String contactId,
                               @NotEmpty String applyInfo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserinfo(request);
        Integer joinType = this.userContactApplyService.applyAdd(tokenUserInfoDto, contactId, applyInfo);
        return getSuccessResponseVO(joinType);
    }

    @GlobalInterceptor
    @RateLimit
    @GetMapping("/loadApply")
    public ResponseVO loadApply(HttpServletRequest request, Integer pageNum) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserinfo(request);
        UserContactApplyQuery userContactApplyQuery = new UserContactApplyQuery();
        userContactApplyQuery.setOrderBy("last_apply_time desc");
        userContactApplyQuery.setReceiverUserId(tokenUserInfoDto.getUserId());
        userContactApplyQuery.setPageNum(pageNum);
        userContactApplyQuery.setPageSize(PageSize.SIZE15.getSize());
        userContactApplyQuery.setQueryContactInfo(true);
        PaginationResultVO resultVO = this.userContactApplyService.findListByPage(userContactApplyQuery);
        return getSuccessResponseVO(resultVO);
    }

    @GlobalInterceptor
    @RateLimit
    @PutMapping("/dealWithApply")
    public ResponseVO dealWithApply(HttpServletRequest request,
                                    @NotNull Integer applyId,
                                    @NotNull Integer status) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserinfo(request);
        this.userContactApplyService.dealWithApply(tokenUserInfoDto.getUserId(), applyId, status);
        return getSuccessResponseVO(null);
    }

    @GlobalInterceptor
    @RateLimit
    @GetMapping("/loadContact")
    public ResponseVO loadContact(HttpServletRequest request,
                                  @NotNull Integer contactType) {
        if (UserContactTypeEnum.getByType(contactType) == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        TokenUserInfoDto tokenUserInfoDto = getTokenUserinfo(request);
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setUserId(tokenUserInfoDto.getUserId());
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            userContactQuery.setQueryContactUserInfo(true);
        } else if (UserContactTypeEnum.GROUP.getType().equals(contactType)) {
            userContactQuery.setQueryGroupInfo(true);
            userContactQuery.setExcludeMyGroup(true);
        }
        userContactQuery.setStatusArray(new int[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.DEL_BY.getStatus(),
                UserContactStatusEnum.BLACKLIST_BY.getStatus()
        });
        userContactQuery.setOrderBy("last_update_time desc");
        List<UserContact> contactList = this.userContactService.findListByParam(userContactQuery);
        return getSuccessResponseVO(contactList);
    }

    /**
     * 获取某个用户信息
     *
     * @param request
     * @param contactId
     * @return
     */
    @GlobalInterceptor
    @RateLimit
    @GetMapping("/getContactInfo")
    public ResponseVO getContactInfo(HttpServletRequest request,
                                     @NotEmpty String contactId) {
        UserContact userContact = this.userContactService.getUserContactByUserIdAndContactId(getTokenUserinfo(request).getUserId(), contactId);
        UserInfo userInfo = this.userInfoService.getUserInfoByUserId(contactId);
        ContactInfoVO userInfoVO = CopyUtils.copy(userInfo, ContactInfoVO.class);
        userInfoVO.setContactStatus(UserContactStatusEnum.NOT_FIEND.getStatus());
        if (userContact != null) {
            userInfoVO.setContactStatus(UserContactStatusEnum.FRIEND.getStatus());
        }
        return getSuccessResponseVO(userInfoVO);
    }

    /**
     * 获取好友信息
     *
     * @param request
     * @param contactId
     * @return
     */
    @GlobalInterceptor
    @RateLimit
    @GetMapping("/getUserContactInfo")
    public ResponseVO getUserContactInfo(HttpServletRequest request,
                                         @NotEmpty String contactId) {
        UserContact userContact = this.userContactService.getUserContactByUserIdAndContactId(getTokenUserinfo(request).getUserId(), contactId);
        if (userContact != null && ArraysUtil.contains(new Integer[]{UserContactStatusEnum.BLACKLIST_BY.getStatus()}, userContact.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        ContactInfoVO contactInfoVO = null;
        if (UserContactTypeEnum.USER.getPrefix().equals(String.valueOf(contactId.charAt(0)))) {
            UserInfo userInfo = this.userInfoService.getUserInfoByUserId(contactId);
            contactInfoVO = CopyUtils.copy(userInfo, ContactInfoVO.class);
        } else {
            GroupInfo groupInfo = this.groupInfoService.getGroupInfoByGroupId(contactId);
            contactInfoVO = CopyUtils.copy(groupInfo, ContactInfoVO.class);
            contactInfoVO.setNickname(groupInfo.getGroupName());
        }
        contactInfoVO.setContactStatus(userContact == null ? UserContactStatusEnum.NOT_FIEND.getStatus() : userContact.getStatus());
        return getSuccessResponseVO(contactInfoVO);
    }

    @GlobalInterceptor
    @RateLimit
    @DeleteMapping("/delUserContact")
    public ResponseVO delUserContact(HttpServletRequest request,
                                     @NotEmpty String contactId) {
        String userId = getTokenUserinfo(request).getUserId();
        UserContact userContact = this.userContactService.getUserContactByUserIdAndContactId(userId, contactId);
        if (userContact == null || !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        this.userContactService.delOrBlacklistUserContact(userId, contactId, UserContactStatusEnum.DEL);
        return getSuccessResponseVO(null);
    }

    @GlobalInterceptor
    @RateLimit
    @PostMapping("/blacklistUserContact")
    public ResponseVO blacklistUserContact(HttpServletRequest request,
                                           @NotEmpty String contactId) {
        String userId = getTokenUserinfo(request).getUserId();
        UserContact userContact = this.userContactService.getUserContactByUserIdAndContactId(userId, contactId);
        if (userContact == null || !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        this.userContactService.delOrBlacklistUserContact(userId, contactId, UserContactStatusEnum.BLACKLIST);
        return getSuccessResponseVO(null);
    }
}