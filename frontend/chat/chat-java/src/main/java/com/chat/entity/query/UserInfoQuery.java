package com.chat.entity.query;

import java.util.Date;

/**
 * @Description:用户信息表
 * @author:normal
 * @Date:2026/01/03 20:04:12
 */
public class UserInfoQuery extends BaseQuery{
	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 密码
	 */
	private String password;

	private String passwordFuzzy;

	/**
	 * 昵称
	 */
	private String nickname;

	private String nicknameFuzzy;

	/**
	 * 被添加方式 0：直接添加  1：需要验证
	 */
	private Integer connectType;

	/**
	 * 邮箱
	 */
	private String email;

	private String emailFuzzy;

	/**
	 * 性别 0：男 1：女
	 */
	private Integer sex;

	/**
	 * 个性签名
	 */
	private String personalSignature;

	private String personalSignatureFuzzy;

	/**
	 * 状态
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	private Date createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 最近登录时间
	 */
	private Date recentLoginTime;

	private String recentLoginTimeStart;

	private String recentLoginTimeEnd;

	/**
	 * 地区名
	 */
	private String areaName;

	private String areaNameFuzzy;

	/**
	 * 地区编号
	 */
	private String areaCode;

	private String areaCodeFuzzy;

	/**
	 * 最近离线时间
	 */
	private Long recentOfflineTime;

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

	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}

	public String getUserIdFuzzy() {
		return this.userIdFuzzy;
	}

	public void setPasswordFuzzy(String passwordFuzzy) {
		this.passwordFuzzy = passwordFuzzy;
	}

	public String getPasswordFuzzy() {
		return this.passwordFuzzy;
	}

	public void setNicknameFuzzy(String nicknameFuzzy) {
		this.nicknameFuzzy = nicknameFuzzy;
	}

	public String getNicknameFuzzy() {
		return this.nicknameFuzzy;
	}

	public void setEmailFuzzy(String emailFuzzy) {
		this.emailFuzzy = emailFuzzy;
	}

	public String getEmailFuzzy() {
		return this.emailFuzzy;
	}

	public void setPersonalSignatureFuzzy(String personalSignatureFuzzy) {
		this.personalSignatureFuzzy = personalSignatureFuzzy;
	}

	public String getPersonalSignatureFuzzy() {
		return this.personalSignatureFuzzy;
	}

	public void setCreateTimeStart(String createTimeStart) {
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart() {
		return this.createTimeStart;
	}

	public void setCreateTimeEnd(String createTimeEnd) {
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd() {
		return this.createTimeEnd;
	}

	public void setRecentLoginTimeStart(String recentLoginTimeStart) {
		this.recentLoginTimeStart = recentLoginTimeStart;
	}

	public String getRecentLoginTimeStart() {
		return this.recentLoginTimeStart;
	}

	public void setRecentLoginTimeEnd(String recentLoginTimeEnd) {
		this.recentLoginTimeEnd = recentLoginTimeEnd;
	}

	public String getRecentLoginTimeEnd() {
		return this.recentLoginTimeEnd;
	}

	public void setAreaNameFuzzy(String areaNameFuzzy) {
		this.areaNameFuzzy = areaNameFuzzy;
	}

	public String getAreaNameFuzzy() {
		return this.areaNameFuzzy;
	}

	public void setAreaCodeFuzzy(String areaCodeFuzzy) {
		this.areaCodeFuzzy = areaCodeFuzzy;
	}

	public String getAreaCodeFuzzy() {
		return this.areaCodeFuzzy;
	}

}