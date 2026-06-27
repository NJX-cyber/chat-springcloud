package com.example.model.exception;


import com.example.model.controller.BaseController;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.enums.ResponseCodeEnum;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.BindException;
import java.util.concurrent.TimeoutException;

@RestControllerAdvice
public class GlobalExceptionHandlerController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandlerController.class);


    @ExceptionHandler(value = Exception.class)
    Object handleException(Exception e, HttpServletRequest request) {
        logger.error("请求错误，请求地址{}，错误信息：", request.getRequestURI(), e);
        ResponseVO ajaxResponse = new ResponseVO();
        if (e instanceof NoHandlerFoundException) {
            ajaxResponse.setCode(ResponseCodeEnum.CODE_404.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_404.getMessage());
            ajaxResponse.setStatus(STATUS_ERROR);
        } else if (e instanceof BusinessException) {
            // 业务错误
            BusinessException businessException = (BusinessException) e;
            ajaxResponse.setCode(businessException.getCode());
            ajaxResponse.setInfo(businessException.getMessage());
            ajaxResponse.setStatus(STATUS_ERROR);
        } else if (e instanceof BindException) {
            // 参数类型异常
            ajaxResponse.setCode(ResponseCodeEnum.CODE_600.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_600.getMessage());
            ajaxResponse.setStatus(STATUS_ERROR);
        } else if (e instanceof DuplicateKeyException) {
            // 主键冲突
            ajaxResponse.setCode(ResponseCodeEnum.CODE_601.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_601.getMessage());
            ajaxResponse.setStatus(STATUS_ERROR);

        } else if (e instanceof ConstraintViolationException) {
            ajaxResponse.setCode(ResponseCodeEnum.CODE_400.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_400.getMessage());
            ajaxResponse.setStatus(STATUS_ERROR);
        } else if (e instanceof CallNotPermittedException) {
            ajaxResponse.setCode(ResponseCodeEnum.CODE_429.getCode());
            ajaxResponse.setInfo("服务暂时不可用，请稍后重试");
            ajaxResponse.setStatus(STATUS_ERROR);
        } else if (e instanceof TimeoutException) {
            ajaxResponse.setCode(ResponseCodeEnum.CODE_429.getCode());
            ajaxResponse.setInfo("请求超时，请稍后重试");
            ajaxResponse.setStatus(STATUS_ERROR);
        } else {
            ajaxResponse.setCode(ResponseCodeEnum.CODE_500.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_500.getMessage());
            ajaxResponse.setStatus(STATUS_ERROR);
        }
        return ajaxResponse;
    }

}
