package com.example.model.entity.query;

import java.util.Date;

/**
 * @Description:app 发布
 * @author:normal
 * @Date:2026/01/23 16:41:07
 */
public class AppUpdateQuery extends BaseQuery{
	/**
	 * 自增ID
	 */
	private Integer id;

	/**
	 * 版本号
	 */
	private String version;

	private String versionFuzzy;

	/**
	 * 更新描述
	 */
	private String updateDesc;

	private String updateDescFuzzy;

	/**
	 * 创建时间
	 */
	private Date createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 发布状态 0：未发布 1：灰度发布 2：全网发布
	 */
	private Integer status;

	/**
	 * 灰度UID
	 */
	private String grayscaleUid;

	private String grayscaleUidFuzzy;

	/**
	 * 文件类型 0：本地文件 1：外链
	 */
	private Integer fileType;

	/**
	 * 外链地址
	 */
	private String outLink;

	private String outLinkFuzzy;

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

	public void setVersionFuzzy(String versionFuzzy) {
		this.versionFuzzy = versionFuzzy;
	}

	public String getVersionFuzzy() {
		return this.versionFuzzy;
	}

	public void setUpdateDescFuzzy(String updateDescFuzzy) {
		this.updateDescFuzzy = updateDescFuzzy;
	}

	public String getUpdateDescFuzzy() {
		return this.updateDescFuzzy;
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

	public void setGrayscaleUidFuzzy(String grayscaleUidFuzzy) {
		this.grayscaleUidFuzzy = grayscaleUidFuzzy;
	}

	public String getGrayscaleUidFuzzy() {
		return this.grayscaleUidFuzzy;
	}

	public void setOutLinkFuzzy(String outLinkFuzzy) {
		this.outLinkFuzzy = outLinkFuzzy;
	}

	public String getOutLinkFuzzy() {
		return this.outLinkFuzzy;
	}

}