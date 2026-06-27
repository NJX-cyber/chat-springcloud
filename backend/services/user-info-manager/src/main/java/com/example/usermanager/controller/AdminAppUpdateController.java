package com.example.usermanager.controller;

import com.example.model.annotation.GlobalInterceptor;
import com.example.model.annotation.RateLimit;
import com.example.model.entity.po.AppUpdate;
import com.example.model.entity.query.AppUpdateQuery;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.controller.BaseController;
import com.example.usermanager.service.AppUpdateService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * author:normal
 * date:2026/1/23 16:52
 * description:
 */

@RestController("adminAppUpdateController")
@RequestMapping("/admin")
@Validated
public class AdminAppUpdateController extends BaseController {

    @Resource
    private AppUpdateService appUpdateService;

    @RateLimit
    @GetMapping("/loadUpdateList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUpdateList(AppUpdateQuery appUpdateQuery) {
        appUpdateQuery.setOrderBy("id desc");
        return getSuccessResponseVO(this.appUpdateService.findListByPage(appUpdateQuery));
    }

    @RateLimit
    @PostMapping("/saveUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveUpdate(Integer id,
                                 @NotEmpty String version,
                                 @NotEmpty String updateDesc,
                                 @NotNull Integer fileType,
                                 String outLink,
                                 MultipartFile file) throws IOException {
        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setId(id);
        appUpdate.setVersion(version);
        appUpdate.setUpdateDesc(updateDesc);
        appUpdate.setFileType(fileType);
        appUpdate.setOutLink(outLink);
        this.appUpdateService.saveUpdate(appUpdate, file);
        return getSuccessResponseVO(null);
    }

    @RateLimit
    @DeleteMapping("/delUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO delUpdate(@NotNull Integer id) {
        this.appUpdateService.deleteAppUpdateById(id);
        return getSuccessResponseVO(null);
    }

    @RateLimit
    @PostMapping("/postUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO postUpdate(@NotNull Integer id,
                                 @NotNull Integer status,
                                 String grayscaleUid) {
        this.appUpdateService.postUpdate(id, status, grayscaleUid);
        return getSuccessResponseVO(null);
    }
}