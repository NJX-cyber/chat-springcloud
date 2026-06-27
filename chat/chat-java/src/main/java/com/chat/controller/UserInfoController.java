package com.chat.controller;

import com.chat.annotation.GlobalInterceptor;
import com.chat.constants.Constants;
import com.chat.entity.dto.TokenUserInfoDto;
import com.chat.entity.po.UserInfo;
import com.chat.entity.vo.ResponseVO;
import com.chat.entity.vo.ContactInfoVO;
import com.chat.enums.ResponseCodeEnum;
import com.chat.enums.UserStatusEnum;
import com.chat.exception.BusinessException;
import com.chat.service.UserInfoService;
import com.chat.utils.CopyUtils;
import com.chat.utils.StringUtils;
import com.chat.websocket.ChannelContextUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.IOException;

/**
 * author:normal
 * date:2026/1/22 10:31
 * description:
 */

@RestController("userInfoController")
@RequestMapping("/userInfo")
@Validated
public class UserInfoController extends BaseController {
    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @GetMapping("/getUserInfo")
    @GlobalInterceptor
    public ResponseVO getUserInfo(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserinfo(request);
        UserInfo userInfo = this.userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
        if (userInfo == null || UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        ContactInfoVO userInfoVO = CopyUtils.copy(userInfo, ContactInfoVO.class);
        userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
        return getSuccessResponseVO(userInfoVO);
    }

    @PutMapping("/updateUserInfo")
    @GlobalInterceptor
    public ResponseVO updateUserInfo(HttpServletRequest request,
                                     UserInfo userInfo,
                                     MultipartFile avatarFile,
                                     MultipartFile avatarCover) throws IOException {
        String userId = getTokenUserinfo(request).getUserId();
        userInfo.setUserId(userId);
        userInfo.setPassword(null);
        userInfo.setStatus(null);
        userInfo.setCreateTime(null);
        userInfo.setRecentLoginTime(null);
        this.userInfoService.updateUserInfo(userInfo, avatarFile, avatarCover);
        return getSuccessResponseVO(null);
    }

    @PutMapping("/updateUserPassword")
    @GlobalInterceptor
    public ResponseVO updateUserPassword(HttpServletRequest request,
                                         @NotEmpty @Pattern(regexp = Constants.PATTERNS_PASSWORD) String password,
                                         @NotEmpty @Pattern(regexp = Constants.PATTERNS_PASSWORD) String newPassword) {
        String userId = getTokenUserinfo(request).getUserId();
        UserInfo dbUserInfo = this.userInfoService.getUserInfoByUserId(userId);
        if (dbUserInfo == null || UserStatusEnum.DISABLE.getStatus().equals(dbUserInfo.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        password = StringUtils.encodeMd5(password);
        if (!password.equals(dbUserInfo.getPassword())) {
            throw new BusinessException("原密码输入错误");
        }
        newPassword = StringUtils.encodeMd5(newPassword);
        if (newPassword.equals(dbUserInfo.getPassword())) {
            throw new BusinessException("新密码不能和原密码一致");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setPassword(newPassword);
        this.userInfoService.updateUserInfoByUserId(userInfo, userId);
        this.channelContextUtils.closeContact(userId);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/logout")
    @GlobalInterceptor
    public ResponseVO logout(HttpServletRequest request) {
        String userId = getTokenUserinfo(request).getUserId();
        this.channelContextUtils.closeContact(userId);
        return getSuccessResponseVO(null);
    }
}
