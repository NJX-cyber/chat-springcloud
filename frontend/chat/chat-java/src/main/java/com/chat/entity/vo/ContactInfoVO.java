package com.chat.entity.vo;

import java.io.Serializable;

/**
 * author:normal
 * date:2026/1/5 15:39
 * description:
 */
public class ContactInfoVO implements Serializable {

    private static final long serialVersionUID = 1569730755808102922L;
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 群组ID
     */
    private String groupId;
    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 被添加方式 0：直接添加  1：需要验证
     */
    private Integer connectType;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别 0：男 1：女
     */
    private Integer sex;

    /**
     * 个性签名
     */
    private String personalSignature;

    /**
     * 地区名
     */
    private String areaName;

    /**
     * 地区编号
     */
    private String areaCode;

    private String token;

    private Boolean admin;

    private Integer contactStatus;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getConnectType() {
        return connectType;
    }

    public void setConnectType(Integer connectType) {
        this.connectType = connectType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getPersonalSignature() {
        return personalSignature;
    }

    public void setPersonalSignature(String personalSignature) {
        this.personalSignature = personalSignature;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Integer getContactStatus() {
        return contactStatus;
    }

    public void setContactStatus(Integer contactStatus) {
        this.contactStatus = contactStatus;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}