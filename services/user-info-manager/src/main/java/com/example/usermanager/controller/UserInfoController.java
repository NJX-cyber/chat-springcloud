package com.example.usermanager.controller;


import com.example.model.annotation.GlobalInterceptor;
import com.example.model.annotation.RateLimit;
import com.example.model.constants.Constants;
import com.example.model.entity.dto.TokenUserInfoDto;
import com.example.model.entity.po.UserInfo;
import com.example.model.entity.vo.ContactInfoVO;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.enums.ResponseCodeEnum;
import com.example.model.enums.UserStatusEnum;
import com.example.model.controller.BaseController;
import com.example.model.exception.BusinessException;
import com.example.model.feign.ChatMessageFeignClient;
import com.example.model.utils.CopyUtils;
import com.example.model.utils.StringUtils;
import com.example.usermanager.service.UserInfoService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    private static final Logger logger = LoggerFactory.getLogger(UserInfoController.class);

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ChatMessageFeignClient chatMessageFeignClient;

    @GlobalInterceptor
    @RateLimit
    @GetMapping("/getUserInfo")
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

    @GlobalInterceptor
    @RateLimit
    @PutMapping("/updateUserInfo")
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

    @GlobalInterceptor
    @RateLimit
    @PutMapping("/updateUserPassword")
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
        try {
            chatMessageFeignClient.closeContact(userId);
        } catch (Exception e) {
            logger.warn("关闭用户连接失败（用户可能不在线）, userId={}", userId, e);
        }
        return getSuccessResponseVO(null);
    }

    @GlobalInterceptor
    @RateLimit
    @PostMapping("/logout")
    public ResponseVO logout(HttpServletRequest request) {
        String userId = getTokenUserinfo(request).getUserId();
        try {
            chatMessageFeignClient.closeContact(userId);
        } catch (Exception e) {
            logger.warn("退出登录关闭连接失败（用户可能不在线）, userId={}", userId, e);
        }
        return getSuccessResponseVO(null);
    }

    @GetMapping("/test")
    public ResponseVO test() {
        return getSuccessResponseVO(null);
    }

}
