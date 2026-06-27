package com.example.model.entity.dto;

import java.io.Serializable;

/**
 * author:normal
 * date:2026/1/5 11:24
 * description:
 */
public class TokenUserInfoDto implements Serializable {

    private static final long serialVersionUID = -3148381603390194690L;

    private String token;
    private String userId;
    private String nickname;
    private Boolean admin;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }
}