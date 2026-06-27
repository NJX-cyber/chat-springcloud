package com.chat.controller;

import com.chat.Redis.RedisUtils;
import com.chat.constants.Constants;
import com.chat.entity.dto.TokenUserInfoDto;
import com.chat.enums.ResponseCodeEnum;;
import com.chat.entity.vo.ResponseVO;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
