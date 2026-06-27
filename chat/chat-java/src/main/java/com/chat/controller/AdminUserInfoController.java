package com.chat.controller;

import com.chat.annotation.GlobalInterceptor;
import com.chat.entity.query.UserInfoQuery;
import com.chat.entity.vo.PaginationResultVO;
import com.chat.entity.vo.ResponseVO;
import com.chat.service.UserInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * author:normal
 * date:2026/1/22 16:51
 * description:
 */

@RestController("adminUserInfoController")
@RequestMapping("/admin")
@Validated
public class AdminUserInfoController extends BaseController {

    @Resource
    private UserInfoService userInfoService;

    @GetMapping("/loadUser")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUser(UserInfoQuery userInfoQuery) {
        userInfoQuery.setOrderBy("create_time desc");
        PaginationResultVO resultVO = this.userInfoService.findListByPage(userInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    @PutMapping("/updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO updateUserStatus(@NotNull Integer status,
                                       @NotEmpty String userId) {
        this.userInfoService.updateUserStatus(status, userId);
        return getSuccessResponseVO(null);
    }

    @PutMapping("/forceOffLine")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO forceOffLine(@NotEmpty String userId) {
        this.userInfoService.forceOffLine(userId);
        return getSuccessResponseVO(null);
    }
}