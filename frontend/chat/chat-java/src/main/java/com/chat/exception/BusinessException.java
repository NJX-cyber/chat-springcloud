package com.chat.exception;

import com.chat.enums.ResponseCodeEnum;;

public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 7344807665051407747L;
    private ResponseCodeEnum codeEnum;

    private Integer code;

    private String message;

    public BusinessException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    public BusinessException(String message) {
        super(message);
        this.message = message;
    }

    public BusinessException(Throwable e) {
        super(e);
    }

    public BusinessException(ResponseCodeEnum codeEnum) {
        super(codeEnum.getMessage());
        this.codeEnum = codeEnum;
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMessage();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public ResponseCodeEnum getCodeEnum() {
        return codeEnum;
    }

    public void setCodeEnum(ResponseCodeEnum codeEnum) {
        this.codeEnum = codeEnum;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 重写fillInStackTrace 业务不需要堆栈信息， 提高效率。
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
