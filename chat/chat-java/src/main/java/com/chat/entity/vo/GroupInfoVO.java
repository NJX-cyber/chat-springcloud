package com.chat.entity.vo;

import com.chat.entity.po.GroupInfo;
import com.chat.entity.po.UserContact;

import java.util.List;

/**
 * author:normal
 * date:2026/1/19 16:15
 * description:
 */
public class GroupInfoVO {
    private GroupInfo groupInfo;
    private List<UserContact> userContactList;

    public GroupInfo getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
    }

    public void setUserContactList(List<UserContact> userContactList) {
        this.userContactList = userContactList;
    }

    public List<UserContact> getUserContactList() {
        return userContactList;
    }

}