package com.chat.enums;

public enum AppUpdateFileTypeEnum {
    LOCAL(0, "本地"), OUT_LINK(1, "外链");
    private Integer type;
    private String desc;

    AppUpdateFileTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static AppUpdateFileTypeEnum getByType(Integer type) {
        for (AppUpdateFileTypeEnum typeEnum : AppUpdateFileTypeEnum.values()) {
            if (typeEnum.getType().equals(type)) {
                return typeEnum;
            }
        }
        return null;
    }
}
