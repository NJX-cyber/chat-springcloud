package com.example.model.enums;

public enum ResponseCodeEnum {
    CODE_200(200, "请求成功"),
    CODE_400(400, "网络出现波动，请重新尝试"),
    CODE_404(404, "请求地址不存在"),
    CODE_429(429, "请求次数过多，请稍后再试"),
    CODE_600(600, "请求参数错误"),
    CODE_601(601, "信息已经存在"),
    CODE_602(602, "文件不存在"),
    CODE_603(603, "文件太大，上传失败"),
    CODE_500(500, "服务器返回错误，请联系管理员"),
    CODE_901(901, "登录超时"),
    CODE_902(902, "你不是对方的好友，请先发送好友申请"),
    CODE_903(903, "你已不在群聊，请重新申请加入");

    private Integer code;

    private String message;

    ResponseCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
