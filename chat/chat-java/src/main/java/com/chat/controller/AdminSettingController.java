package com.chat.controller;

import com.chat.Redis.RedisComponent;
import com.chat.annotation.GlobalInterceptor;
import com.chat.constants.Constants;
import com.chat.entity.config.AppConfig;
import com.chat.entity.dto.SystemSettingsDto;
import com.chat.entity.vo.ResponseVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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

    @GetMapping("/getSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO getSysSettings() {
        return getSuccessResponseVO(this.redisComponent.getSystemSettings());
    }

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