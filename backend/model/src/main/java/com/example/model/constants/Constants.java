package com.example.model.constants;

import com.example.model.enums.UserContactTypeEnum;

/**
 * author:normal
 * date:2026/1/4 15:24
 * description:
 */
public class Constants {
    public static final String REDIS_KEY_CHECK_CODE = "chat:checkCode:";
    public static final String REDIS_KEY_WS_USER_HEART_BEAT = "chat:ws:user:heartbeat:";
    public static final String REDIS_KEY_WS_USER_TOKEN = "chat:ws:user:token:";
    public static final String REDIS_KEY_WS_USER_ID = "chat:ws:user:id:";
    public static final String REDIS_KEY_WS_USER_CONTACT = "chat:ws:user:contact";
    public static final Integer REDIS_KEY_EXPIRES_HEART_BEAT = 6;
    public static final Integer REDIS_TIME_ONE_MIN = 60;
    public static final Integer REDIS_TIME_ONE_DAY = REDIS_TIME_ONE_MIN * 60 * 24;
    public static final Integer REDIS_TIME_FIVE_MIN = REDIS_TIME_ONE_MIN * 5;
    public static final Integer REDIS_TIME_TEN_MIN = REDIS_TIME_ONE_MIN * 10;

    // ==== 安全相关 Redis Key ====
    /** 验证码请求频控: chat:captcha:ip:{ip} */
    public static final String REDIS_KEY_CAPTCHA_IP = "chat:captcha:ip:";
    /** 登录失败计数: chat:loginFail:{ip} */
    public static final String REDIS_KEY_LOGIN_FAIL = "chat:loginFail:";
    /** 登录锁定标记: chat:loginLock:{ip} */
    public static final String REDIS_KEY_LOGIN_LOCK = "chat:loginLock:";
    /** 注册频控: chat:register:ip:{ip} */
    public static final String REDIS_KEY_REGISTER_IP = "chat:register:ip:";

    /** 验证码同IP最小间隔（秒） */
    public static final Integer CAPTCHA_IP_INTERVAL = 1;
    /** 登录连续失败上限 */
    public static final Integer MAX_LOGIN_FAIL_COUNT = 5;
    /** 登录锁定时间（秒） */
    public static final Integer LOGIN_LOCK_DURATION = REDIS_TIME_TEN_MIN;


    public static final Long MILLIS_SECONDS_3DAY_AGO = 3 * 24 * 60 * 60 * 1000L;

    public static final Integer LENGTH_ELEVEN = 11;
    public static final Integer LENGTH_TWENTY = 20;

    public static final String ROBOT_UID = UserContactTypeEnum.USER.getPrefix() + "robot";

    public static final String REDIS_KEY_SYSTEM_SETTINGS = "chat:systemSettings";

    public static final String FILE_FOLDER_FILE = "/file/";
    public static final String FILE_FOLDER_AVATAR = "avatar/";
    public static final String IMG_SUFFIX = ".png";
    public static final String COVER_IMG_SUFFIX = "_cover.png";

    public static final Long FILE_SIZE_MB = 1024 * 1024L;

    public static final String[] IMAGE_SUFFIX_LIST = new String[]{".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
    public static final String[] VIDEO_SUFFIX_LIST = new String[]{".mp4", ".avi", ".rmvb", ".mkv", ".mov"};

    public static final String APPLY_INFO_DEFAULT = "我是%s";

    public static final String PATTERNS_PASSWORD = "^[a-fA-F0-9]{32}$";

    public static final String APP_UPDATE_FOLDER = "/app/";
    public static final String APP_EXE_SUFFIX = ".exe";
    public static final String APP_NAME = "ChatSetup";
}