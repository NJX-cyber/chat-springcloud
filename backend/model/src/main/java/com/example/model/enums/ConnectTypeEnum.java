package com.example.model.enums;

public enum ConnectTypeEnum {
    DIRECT_ADD(0,"直接添加"),NEED_VERIFY(1,"需要验证");

    private Integer Type;

    private String desc;

    ConnectTypeEnum(Integer Type, String desc) {
        this.Type = Type;
        this.desc = desc;
    }

    public Integer getType() {
        return Type;
    }

    public String getDesc() {
        return desc;
    }

    public static ConnectTypeEnum getByStatus(Integer type) {
        for (ConnectTypeEnum item : ConnectTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }
}
