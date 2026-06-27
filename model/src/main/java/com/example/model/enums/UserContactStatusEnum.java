package com.example.model.enums;

public enum UserContactStatusEnum {
    NOT_FIEND(0, "非好友"),
    FRIEND(1, "好友"),
    DEL(2, "已删除好友"),
    DEL_BY(3, "被好友删除"),
    BLACKLIST(4, "加入黑名单"),
    BLACKLIST_BY(5, "被加入黑名单"),
    BLACKLIST_BY_FIRST(6, "添加时被加入黑名单");

    private Integer status;

    private String desc;

    UserContactStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static UserContactStatusEnum getByStatus(Integer status) {
        for (UserContactStatusEnum item : UserContactStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
