package com.example.model.entity.po;

import java.io.Serializable;

/**
 * @Description:
 * @author:normal
 * @Date:2026/01/26 14:34:11
 */
public class ChatSession implements Serializable {
	/**
	 * 会话ID
	 */
	private String sessionId;

	/**
	 * 最后接收的消息
	 */
	private String lastMessage;

	/**
	 * 最后接收消息时间毫秒
	 */
	private Long lastReceiveTime;

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public String getLastMessage() {
		return this.lastMessage;
	}

	public void setLastReceiveTime(Long lastReceiveTime) {
		this.lastReceiveTime = lastReceiveTime;
	}

	public Long getLastReceiveTime() {
		return this.lastReceiveTime;
	}

	@Override
	public String toString() {
		return "UserInfo{会话ID:" + (sessionId == null ? "空值" : sessionId) + ", 最后接收的消息:" + (lastMessage == null ? "空值" : lastMessage) + ", 最后接收消息时间毫秒:" + (lastReceiveTime == null ? "空值" : lastReceiveTime) + "}";
	}
}