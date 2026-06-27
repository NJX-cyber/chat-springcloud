package com.chat.controller;

import com.chat.annotation.GlobalInterceptor;
import com.chat.entity.po.UserInfoBeauty;
import com.chat.entity.query.UserInfoBeautyQuery;
import com.chat.entity.vo.PaginationResultVO;
import com.chat.entity.vo.ResponseVO;
import com.chat.service.UserInfoBeautyService;
import com.sun.istack.internal.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * author:normal
 * date:2026/1/22 17:10
 * description:
 */

@RestController("adminUserInfoBeautyController")
@RequestMapping("/admin")
@Validated
public class AdminUserInfoBeautyController extends BaseController {
    @Resource
    private UserInfoBeautyService userInfoBeautyService;

    @GetMapping("/loadBeautyAccountList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadBeautyAccountList(UserInfoBeautyQuery userInfoBeautyQuery) {
        userInfoBeautyQuery.setOrderBy("id desc");
        PaginationResultVO<UserInfoBeauty> listByPage = this.userInfoBeautyService.findListByPage(userInfoBeautyQuery);
        return  getSuccessResponseVO(listByPage);
    }

    @PostMapping("/saveBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveBeautyAccount(UserInfoBeauty userInfoBeauty) {
        this.userInfoBeautyService.saveBeautyAccount(userInfoBeauty);
        return  getSuccessResponseVO(null);
    }

    @DeleteMapping("/delBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO delBeautyAccount(@NotNull Integer id) {
        this.userInfoBeautyService.deleteUserInfoBeautyById(id);
        return  getSuccessResponseVO(null);
    }
}