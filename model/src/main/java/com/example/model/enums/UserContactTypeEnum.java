package com.example.model.enums;

import com.example.model.utils.StringUtils;

public enum UserContactTypeEnum {
    USER(0, "U", "好友"), GROUP(1, "G", "群组");

    private Integer type;

    private String prefix;

    private String desc;

    UserContactTypeEnum(Integer type, String prefix, String desc) {
        this.type = type;
        this.prefix = prefix;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDesc() {
        return desc;
    }

    public static UserContactTypeEnum getByType(Integer type) {
        for (UserContactTypeEnum item : UserContactTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }

    public static UserContactTypeEnum getByName(String name) {
        try {
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            return UserContactTypeEnum.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public static UserContactTypeEnum getByPrefix(String prefix) {
        try {
            if (StringUtils.isEmpty(prefix)) {
                return null;
            }
            prefix = prefix.substring(0, 1).toUpperCase();
            for (UserContactTypeEnum item : UserContactTypeEnum.values()) {
                if (item.getPrefix().equals(prefix)) {
                    return item;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
