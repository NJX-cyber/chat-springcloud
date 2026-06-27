package com.chat.controller;

import com.chat.annotation.GlobalInterceptor;
import com.chat.entity.po.AppUpdate;
import com.chat.entity.query.AppUpdateQuery;
import com.chat.entity.vo.ResponseVO;
import com.chat.service.AppUpdateService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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

    @GetMapping("/loadUpdateList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUpdateList(AppUpdateQuery appUpdateQuery) {
        appUpdateQuery.setOrderBy("id desc");
        return getSuccessResponseVO(this.appUpdateService.findListByPage(appUpdateQuery));
    }

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

    @DeleteMapping("/delUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO delUpdate(@NotNull Integer id) {
        this.appUpdateService.deleteAppUpdateById(id);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/postUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO postUpdate(@NotNull Integer id,
                                 @NotNull Integer status,
                                 String grayscaleUid) {
        this.appUpdateService.postUpdate(id, status, grayscaleUid);
        return getSuccessResponseVO(null);
    }
}