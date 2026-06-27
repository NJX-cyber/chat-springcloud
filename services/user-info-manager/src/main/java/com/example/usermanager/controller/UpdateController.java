package com.example.usermanager.controller;

import com.example.model.annotation.GlobalInterceptor;
import com.example.model.annotation.RateLimit;
import com.example.model.config.AppConfig;
import com.example.model.constants.Constants;
import com.example.model.entity.po.AppUpdate;
import com.example.model.entity.vo.AppUpdateVO;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.enums.AppUpdateFileTypeEnum;
import com.example.model.controller.BaseController;
import com.example.model.utils.CopyUtils;
import com.example.model.utils.StringUtils;
import com.example.usermanager.service.AppUpdateService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import java.util.Arrays;

/**
 * author:normal
 * date:2026/1/24 20:12
 * description:
 */

@RestController("updateController")
@RequestMapping("/systemVersion")
public class UpdateController extends BaseController {

    @Resource
    private AppUpdateService appUpdateService;

    @Resource
    private AppConfig appConfig;

    @GetMapping("/checkVersion")
    @RateLimit
    @GlobalInterceptor
    public ResponseVO checkVersion(String appVersion, String userId) {
        if (StringUtils.isEmpty(appVersion)) {
            return getSuccessResponseVO(null);
        }

        AppUpdate latestUpdate = this.appUpdateService.getLatestUpdate(appVersion, userId);

        if (latestUpdate == null) {
            return getSuccessResponseVO(null);
        }

        AppUpdateVO appUpdateVO = CopyUtils.copy(latestUpdate, AppUpdateVO.class);
        if (AppUpdateFileTypeEnum.OUT_LINK.getType().equals(latestUpdate.getFileType())) {
            appUpdateVO.setSize(0L);
        } else {
            File file = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + userId + "/" + Constants.APP_EXE_SUFFIX);
        }
        appUpdateVO.setUpdateList(Arrays.asList
                (latestUpdate.getUpdateDescArray()));
        String appName = Constants.APP_NAME + latestUpdate.getVersion() + Constants.APP_EXE_SUFFIX;
        appUpdateVO.setFileName(appName);
        return getSuccessResponseVO(appUpdateVO);
    }
}