package com.chat.enums;

public enum UserStatusEnum {
    ENABLE(0, "启用"), DISABLE(1, "禁用");
    private Integer status;
    private String desc;

    UserStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static UserStatusEnum getByStatus(Integer status){
        for (UserStatusEnum item : UserStatusEnum.values()) {
            if (item.getStatus().equals(status)){
                return item;
            }
        }
        return null;
    }
}
