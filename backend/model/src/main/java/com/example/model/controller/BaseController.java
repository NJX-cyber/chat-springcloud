package com.example.model.controller;


import com.example.model.Redis.RedisUtils;
import com.example.model.constants.Constants;
import com.example.model.entity.dto.TokenUserInfoDto;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.enums.ResponseCodeEnum;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;


public class BaseController {
    protected static final String STATUS_SUCCESS = "success";

    protected static final String STATUS_ERROR = "error";

    @Resource
    private RedisUtils redisUtils;

    protected <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUS_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMessage());
        responseVO.setData(t);
        return responseVO;
    }

    protected TokenUserInfoDto getTokenUserinfo(HttpServletRequest request) {
        String token = request.getHeader("token");
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_USER_TOKEN + token);
    }
}
