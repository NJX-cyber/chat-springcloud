package com.example.usermanager.controller;

import com.example.model.annotation.GlobalInterceptor;
import com.example.model.annotation.RateLimit;
import com.example.model.entity.po.UserInfoBeauty;
import com.example.model.entity.query.UserInfoBeautyQuery;
import com.example.model.entity.vo.PaginationResultVO;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.controller.BaseController;
import com.example.usermanager.service.UserInfoBeautyService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


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

    @RateLimit
    @GetMapping("/loadBeautyAccountList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadBeautyAccountList(UserInfoBeautyQuery userInfoBeautyQuery) {
        userInfoBeautyQuery.setOrderBy("id desc");
        PaginationResultVO<UserInfoBeauty> listByPage = this.userInfoBeautyService.findListByPage(userInfoBeautyQuery);
        return  getSuccessResponseVO(listByPage);
    }

    @RateLimit
    @PostMapping("/saveBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveBeautyAccount(UserInfoBeauty userInfoBeauty) {
        this.userInfoBeautyService.saveBeautyAccount(userInfoBeauty);
        return  getSuccessResponseVO(null);
    }

    @RateLimit
    @DeleteMapping("/delBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO delBeautyAccount(@NotNull Integer id) {
        this.userInfoBeautyService.deleteUserInfoBeautyById(id);
        return  getSuccessResponseVO(null);
    }
}