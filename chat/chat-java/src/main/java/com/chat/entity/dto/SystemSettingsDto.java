package com.chat.entity.dto;

import com.chat.constants.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * author:normal
 * date:2026/1/5 20:43
 * description:
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemSettingsDto implements Serializable {
    private static final long serialVersionUID = -3200086504753376057L;

    // 最大群组数
    private Integer maxGroupCount = 5;
    // 群组最大人数
    private Integer maxGroupMemberCount = 500;
    // 最大图片大小
    private Integer maxImageSize = 2;
    // 最大视频大小
    private Integer maxVideoSize = 100;
    // 最大文件大小
    private Integer maxFileSize = 100;
    // 机器人ID
    private String robotUid = Constants.ROBOT_UID;
    // 机器人名称
    private String robotNickName = "chat";
    // 欢迎语
    private String robotWelcome = "欢迎使用chat";

    public String getRobotUid() {
        return robotUid;
    }

    public Integer getMaxGroupCount() {
        return maxGroupCount;
    }

    public void setMaxGroupCount(Integer maxGroupCount) {
        this.maxGroupCount = maxGroupCount;
    }

    public Integer getMaxGroupMemberCount() {
        return maxGroupMemberCount;
    }

    public void setMaxGroupMemberCount(Integer maxGroupMemberCount) {
        this.maxGroupMemberCount = maxGroupMemberCount;
    }

    public Integer getMaxImageSize() {
        return maxImageSize;
    }

    public void setMaxImageSize(Integer maxImageSize) {
        this.maxImageSize = maxImageSize;
    }

    public Integer getMaxVideoSize() {
        return maxVideoSize;
    }

    public void setMaxVideoSize(Integer maxVideoSize) {
        this.maxVideoSize = maxVideoSize;
    }

    public Integer getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Integer maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String getRobotNickName() {
        return robotNickName;
    }

    public void setRobotNickName(String robotNickName) {
        this.robotNickName = robotNickName;
    }

    public String getRobotWelcome() {
        return robotWelcome;
    }

    public void setRobotWelcome(String robotWelcome) {
        this.robotWelcome = robotWelcome;
    }
}