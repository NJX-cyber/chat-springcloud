package com.chat.controller;

import com.chat.annotation.GlobalInterceptor;
import com.chat.constants.Constants;
import com.chat.entity.config.AppConfig;
import com.chat.entity.po.AppUpdate;
import com.chat.entity.vo.AppUpdateVO;
import com.chat.entity.vo.ResponseVO;
import com.chat.enums.AppUpdateFileTypeEnum;
import com.chat.service.AppUpdateService;
import com.chat.utils.CopyUtils;
import com.chat.utils.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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