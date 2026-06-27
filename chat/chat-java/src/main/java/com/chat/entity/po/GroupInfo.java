package com.chat.entity.po;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.chat.enums.DateTimePatternEnum;
import com.chat.utils.DateUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @Description:群组
 * @author:normal
 * @Date:2026/01/06 14:45:42
 */
public class GroupInfo implements Serializable {

	private static final long serialVersionUID = -48823411870273748L;
	/**
	 * 群组ID
	 */
	private String groupId;

	/**
	 * 群名
	 */
	private String groupName;

	/**
	 * 群主ID
	 */
	private String groupOwnerId;

	/**
	 * 群人数
	 */
	private Integer memberCount;

	/**
	 * 群主名
	 */
	private String groupOwnerNickName;

	public String getGroupOwnerNickName() {
		return groupOwnerNickName;
	}

	public void setGroupOwnerNickName(String groupOwnerNickName) {
		this.groupOwnerNickName = groupOwnerNickName;
	}

	public Integer getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(Integer memberCount) {
		this.memberCount = memberCount;
	}

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private Date createTime;

	/**
	 * 群公告
	 */
	private String groupNotice;

	/**
	 * 加入方式 0:直接加入，1:需要管理员同意
	 */
	private Integer joinType;

	/**
	 * 状态 0:正常，1:解散
	 */
	private Integer status;

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupName() {
		return this.groupName;
	}

	public void setGroupOwnerId(String groupOwnerId) {
		this.groupOwnerId = groupOwnerId;
	}

	public String getGroupOwnerId() {
		return this.groupOwnerId;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getCreateTime() {
		return this.createTime;
	}

	public void setGroupNotice(String groupNotice) {
		this.groupNotice = groupNotice;
	}

	public String getGroupNotice() {
		return this.groupNotice;
	}

	public void setJoinType(Integer joinType) {
		this.joinType = joinType;
	}

	public Integer getJoinType() {
		return this.joinType;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus() {
		return this.status;
	}

	@Override
	public String toString() {
		return "UserInfo{群组ID:" + (groupId == null ? "空值" : groupId) + ", 群名:" + (groupName == null ? "空值" : groupName) + ", 群主ID:" + (groupOwnerId == null ? "空值" : groupOwnerId) + ", 创建时间:" + (createTime == null ? "空值" : DateUtils.format(createTime,DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ", 群公告:" + (groupNotice == null ? "空值" : groupNotice) + ", 加入方式 0:直接加入，1:需要管理员同意:" + (joinType == null ? "空值" : joinType) + ", 状态 0:正常，1:解散:" + (status == null ? "空值" : status) + "}";
	}
}