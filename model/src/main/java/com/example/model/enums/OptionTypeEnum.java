package com.example.model.enums;

public enum OptionTypeEnum {
    ZERO(0,""),
    ONE(1,"");

    private Integer type;
    private String desc;

    OptionTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
