package com.chat.entity.po;

import java.io.Serializable;

import com.chat.enums.UserContactApplyStatusEnum;

/**
 * @Description:申请联系人
 * @author:normal
 * @Date:2026/01/06 14:45:42
 */
public class UserContactApply implements Serializable {

    private static final long serialVersionUID = 8866557661709266947L;
    /**
     * 好友申请ID
     */
    private Integer applyId;

    /**
     * 申请人ID
     */
    private String applyUserId;

    /**
     * 接收人ID
     */
    private String receiverUserId;

    /**
     * 联系人类型 0:好友 1:群组
     */
    private Integer contactType;

    /**
     * 联系人ID
     */
    private String contactId;

    /**
     * 最后申请时间
     */
    private Long lastApplyTime;

    /**
     * 状态 0:待处理 1:已同意 2:已拒绝 3:已拉黑
     */
    private Integer status;

    /**
     * 申请信息
     */
    private String applyInfo;

    private String contactName;

    public String getStatusName() {
        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        return statusEnum == null ? null : statusEnum.getDesc();
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setApplyId(Integer applyId) {
        this.applyId = applyId;
    }

    public Integer getApplyId() {
        return this.applyId;
    }

    public void setApplyUserId(String applyUserId) {
        this.applyUserId = applyUserId;
    }

    public String getApplyUserId() {
        return this.applyUserId;
    }

    public void setReceiverUserId(String receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public String getReceiverUserId() {
        return this.receiverUserId;
    }

    public void setContactType(Integer contactType) {
        this.contactType = contactType;
    }

    public Integer getContactType() {
        return this.contactType;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getContactId() {
        return this.contactId;
    }

    public void setLastApplyTime(Long lastApplyTime) {
        this.lastApplyTime = lastApplyTime;
    }

    public Long getLastApplyTime() {
        return this.lastApplyTime;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setApplyInfo(String applyInfo) {
        this.applyInfo = applyInfo;
    }

    public String getApplyInfo() {
        return this.applyInfo;
    }

    @Override
    public String toString() {
        return "UserInfo{好友申请ID:" + (applyId == null ? "空值" : applyId) + ", 申请人ID:" + (applyUserId == null ? "空值" : applyUserId) + ", 接收人ID:" + (receiverUserId == null ? "空值" : receiverUserId) + ", 联系人类型 0:好友 1:群组:" + (contactType == null ? "空值" : contactType) + ", 联系人ID:" + (contactId == null ? "空值" : contactId) + ", 最后申请时间:" + (lastApplyTime == null ? "空值" : lastApplyTime) + ", 状态 0:待处理 1:已同意 2:已拒绝 3:已拉黑:" + (status == null ? "空值" : status) + ", 申请信息:" + (applyInfo == null ? "空值" : applyInfo) + "}";
    }
}