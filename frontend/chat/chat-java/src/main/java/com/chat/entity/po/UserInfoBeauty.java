package com.chat.entity.po;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @Description:用户靓号表
 * @author:normal
 * @Date:2026/01/03 20:04:12
 */
public class UserInfoBeauty implements Serializable {

	private static final long serialVersionUID = 8853197674906785361L;
	/**
	 * 自增ID
	 */
	private Integer id;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 0：未使用  1：已使用
	 */
	private Integer status;

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return this.id;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return this.email;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus() {
		return this.status;
	}

	@Override
	public String toString() {
		return "UserInfo{自增ID:" + (id == null ? "空值" : id) + ", 用户ID:" + (userId == null ? "空值" : userId) + ", 邮箱:" + (email == null ? "空值" : email) + ", 0：未使用  1：已使用:" + (status == null ? "空值" : status) + "}";
	}
}