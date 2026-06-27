package com.example.usermanager.controller;

import com.example.model.annotation.GlobalInterceptor;
import com.example.model.annotation.RateLimit;
import com.example.model.entity.query.UserInfoQuery;
import com.example.model.entity.vo.PaginationResultVO;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.controller.BaseController;
import com.example.usermanager.service.UserInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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

    @RateLimit
    @GetMapping("/loadUser")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUser(UserInfoQuery userInfoQuery) {
        userInfoQuery.setOrderBy("create_time desc");
        PaginationResultVO resultVO = this.userInfoService.findListByPage(userInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RateLimit
    @PutMapping("/updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO updateUserStatus(@NotNull Integer status,
                                       @NotEmpty String userId) {
        this.userInfoService.updateUserStatus(status, userId);
        return getSuccessResponseVO(null);
    }

    @RateLimit
    @PutMapping("/forceOffLine")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO forceOffLine(@NotEmpty String userId) {
        this.userInfoService.forceOffLine(userId);
        return getSuccessResponseVO(null);
    }
}