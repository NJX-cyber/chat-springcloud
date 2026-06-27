package com.chat.entity.po;

import java.io.Serializable;
import java.util.Date;

import com.chat.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.chat.enums.DateTimePatternEnum;
import com.chat.utils.DateUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @Description:app 发布
 * @author:normal
 * @Date:2026/01/23 16:41:07
 */
public class AppUpdate implements Serializable {
	/**
	 * 自增ID
	 */
	private Integer id;

	/**
	 * 版本号
	 */
	private String version;

	/**
	 * 更新描述
	 */
	private String updateDesc;

	private String[] updateDescArray;

	public String[] getUpdateDescArray() {
		if (!StringUtils.isEmpty(this.updateDesc)) {
			return this.updateDesc.split("\\|");
		}
		return updateDescArray;
	}

	public void setUpdateDescArray(String[] updateDescArray) {
		this.updateDescArray = updateDescArray;
	}

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private Date createTime;

	/**
	 * 发布状态 0：未发布 1：灰度发布 2：全网发布
	 */
	private Integer status;

	/**
	 * 灰度UID
	 */
	private String grayscaleUid;

	/**
	 * 文件类型 0：本地文件 1：外链
	 */
	private Integer fileType;

	/**
	 * 外链地址
	 */
	private String outLink;

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return this.id;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return this.version;
	}

	public void setUpdateDesc(String updateDesc) {
		this.updateDesc = updateDesc;
	}

	public String getUpdateDesc() {
		return this.updateDesc;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getCreateTime() {
		return this.createTime;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus() {
		return this.status;
	}

	public void setGrayscaleUid(String grayscaleUid) {
		this.grayscaleUid = grayscaleUid;
	}

	public String getGrayscaleUid() {
		return this.grayscaleUid;
	}

	public void setFileType(Integer fileType) {
		this.fileType = fileType;
	}

	public Integer getFileType() {
		return this.fileType;
	}

	public void setOutLink(String outLink) {
		this.outLink = outLink;
	}

	public String getOutLink() {
		return this.outLink;
	}

	@Override
	public String toString() {
		return "UserInfo{自增ID:" + (id == null ? "空值" : id) + ", 版本号:" + (version == null ? "空值" : version) + ", 更新描述:" + (updateDesc == null ? "空值" : updateDesc) + ", 创建时间:" + (createTime == null ? "空值" : DateUtils.format(createTime,DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ", 发布状态 0：未发布 1：灰度发布 2：全网发布:" + (status == null ? "空值" : status) + ", 灰度UID:" + (grayscaleUid == null ? "空值" : grayscaleUid) + ", 文件类型 0：本地文件 1：外链:" + (fileType == null ? "空值" : fileType) + ", 外链地址:" + (outLink == null ? "空值" : outLink) + "}";
	}
}