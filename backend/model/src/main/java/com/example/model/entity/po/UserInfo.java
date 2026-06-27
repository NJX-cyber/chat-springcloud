package com.example.model.entity.po;

import com.example.model.enums.DateTimePatternEnum;
import com.example.model.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description:用户信息表
 * @author:normal
 * @Date:2026/01/03 20:04:12
 */
public class UserInfo implements Serializable {

	private static final long serialVersionUID = 1684966024254904596L;
	/**
	 * 用户ID
	 */
	private String userId;

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
	 * 状态
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private Date createTime;

	/**
	 * 最近登录时间
	 */
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private Date recentLoginTime;

	/**
	 * 地区名
	 */
	private String areaName;

	/**
	 * 地区编号
	 */
	private String areaCode;

	/**
	 * 最近离线时间
	 */
	private Long recentOfflineTime;

	private Boolean onlineType;

	public Boolean getOnlineType() {
		if (recentLoginTime != null && recentLoginTime.getTime() > recentOfflineTime) {
			// System.out.println("time:" + (recentLoginTime.getTime() - recentOfflineTime));
			return true;
		}
		return false;
	}

	public void setOnlineType(Boolean onlineType) {
		this.onlineType = onlineType;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return this.password;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getNickname() {
		return this.nickname;
	}

	public void setConnectType(Integer connectType) {
		this.connectType = connectType;
	}

	public Integer getConnectType() {
		return this.connectType;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return this.email;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public Integer getSex() {
		return this.sex;
	}

	public void setPersonalSignature(String personalSignature) {
		this.personalSignature = personalSignature;
	}

	public String getPersonalSignature() {
		return this.personalSignature;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus() {
		return this.status;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getCreateTime() {
		return this.createTime;
	}

	public void setRecentLoginTime(Date recentLoginTime) {
		this.recentLoginTime = recentLoginTime;
	}

	public Date getRecentLoginTime() {
		return this.recentLoginTime;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getAreaName() {
		return this.areaName;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getAreaCode() {
		return this.areaCode;
	}

	public void setRecentOfflineTime(Long recentOfflineTime) {
		this.recentOfflineTime = recentOfflineTime;
	}

	public Long getRecentOfflineTime() {
		return this.recentOfflineTime;
	}

	@Override
	public String toString() {
		return "UserInfo{用户ID:" + (userId == null ? "空值" : userId) + ", 密码:" + (password == null ? "空值" : password) + ", 昵称:" + (nickname == null ? "空值" : nickname) + ", 被添加方式 0：直接添加  1：需要验证:" + (connectType == null ? "空值" : connectType) + ", 邮箱:" + (email == null ? "空值" : email) + ", 性别 0：男 1：女:" + (sex == null ? "空值" : sex) + ", 个性签名:" + (personalSignature == null ? "空值" : personalSignature) + ", 状态:" + (status == null ? "空值" : status) + ", 创建时间:" + (createTime == null ? "空值" : DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ", 最近登录时间:" + (recentLoginTime == null ? "空值" : DateUtils.format(recentLoginTime,DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ", 地区名:" + (areaName == null ? "空值" : areaName) + ", 地区编号:" + (areaCode == null ? "空值" : areaCode) + ", 最近离线时间:" + (recentOfflineTime == null ? "空值" : recentOfflineTime) + "}";
	}
}