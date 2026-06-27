package com.chat.entity.config;

import com.chat.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * author:normal
 * date:2026/1/5 11:28
 * description:
 */
@Component("appConfig")
public class AppConfig {
    /**
     * websorcket端口
     */
    @Value("${ws.port:}")
    private Integer wsPort;

    /**
     * 项目目录
     */
    @Value("${project.folder:}")
    private String projectFolder;

    /**
     * 超级管理员ID
     */
    @Value("${admin.emails:}")
    private String adminEmails;

    public Integer getWsPort() {
        return wsPort;
    }

    public String getProjectFolder() {
        if (StringUtils.isEmpty(projectFolder) || !projectFolder.endsWith("/")) {
            projectFolder += "/";
        }
        return projectFolder;
    }

    public String getAdminEmails() {
        return adminEmails;
    }
}