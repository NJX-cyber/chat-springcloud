package com.example.model.entity.po;

import com.example.model.enums.UserContactTypeEnum;
import com.example.model.utils.StringUtils;

import java.io.Serializable;

/**
 * @Description:
 * @author:normal
 * @Date:2026/01/26 14:34:11
 */
public class ChatSessionUser implements Serializable {
	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 联系人ID
	 */
	private String contactId;

	/**
	 * 会话ID
	 */
	private String sessionId;

	/**
	 * 联系人名称
	 */
	private String contactName;

	private String lastMessage;

	private Long lastReceiveTime;

	private Integer memberCount;

	private Integer contactType;

	public Integer getContactType() {
		if (StringUtils.isEmpty(contactId)) {
			return null;
		}
		return UserContactTypeEnum.getByPrefix(contactId).getType();
	}

	public void setContactType(Integer contactType) {
		this.contactType = contactType;
	}

	public Integer getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(Integer memberCount) {
		this.memberCount = memberCount;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public Long getLastReceiveTime() {
		return lastReceiveTime;
	}

	public void setLastReceiveTime(Long lastReceiveTime) {
		this.lastReceiveTime = lastReceiveTime;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getContactId() {
		return this.contactId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactName() {
		return this.contactName;
	}

	@Override
	public String toString() {
		return "UserInfo{用户ID:" + (userId == null ? "空值" : userId) + ", 联系人ID:" + (contactId == null ? "空值" : contactId) + ", 会话ID:" + (sessionId == null ? "空值" : sessionId) + ", 联系人名称:" + (contactName == null ? "空值" : contactName) + "}";
	}
}