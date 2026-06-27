package com.example.usermanager.controller;

import com.example.model.Redis.RedisComponent;
import com.example.model.annotation.GlobalInterceptor;
import com.example.model.annotation.RateLimit;
import com.example.model.config.AppConfig;
import com.example.model.constants.Constants;
import com.example.model.entity.dto.SystemSettingsDto;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.controller.BaseController;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * author:normal
 * date:2026/1/23 15:44
 * description:
 */
@RestController("adminSettingController")
@RequestMapping("/admin")
public class AdminSettingController extends BaseController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    @RateLimit
    @GetMapping("/getSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO getSysSettings() {
        return getSuccessResponseVO(this.redisComponent.getSystemSettings());
    }

    @RateLimit
    @PostMapping("/saveSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveSysSetting(SystemSettingsDto systemSettingsDto,
                                     MultipartFile avatarFile,
                                     MultipartFile coverFile) throws IOException {
        if (avatarFile != null) {
            String baseFile = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File targetFileFold = new File(baseFile + Constants.FILE_FOLDER_AVATAR);
            if (!targetFileFold.exists()) {
                targetFileFold.mkdirs();
            }
            String targetPath = targetFileFold.getPath() + "/" + Constants.ROBOT_UID;
            avatarFile.transferTo(new File(targetPath + Constants.IMG_SUFFIX));
            coverFile.transferTo(new File(targetPath + Constants.COVER_IMG_SUFFIX));
        }
        this.redisComponent.saveSystemSettings(systemSettingsDto);
        return getSuccessResponseVO(null);
    }

}