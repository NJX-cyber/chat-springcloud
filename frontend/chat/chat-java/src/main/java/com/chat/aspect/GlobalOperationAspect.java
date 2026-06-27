package com.chat.aspect;

import com.chat.Redis.RedisUtils;
import com.chat.annotation.GlobalInterceptor;
import com.chat.constants.Constants;
import com.chat.entity.dto.TokenUserInfoDto;
import com.chat.enums.ResponseCodeEnum;
import com.chat.exception.BusinessException;
import com.chat.utils.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;

/**
 * author:normal
 * date:2026/1/6 15:34
 * description:
 */
@Aspect
@Component("globalOperationAspect")
public class GlobalOperationAspect {

    @Resource
    private RedisUtils redisUtils;

    private static final Logger logger = LoggerFactory.getLogger(GlobalOperationAspect.class);

    @Before("@annotation(com.chat.annotation.GlobalInterceptor)")
    public void interceptor(JoinPoint point) {
        try {
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);

            if (interceptor == null) {
                return;
            }
            if (interceptor.checkLogin() || interceptor.checkAdmin()) {
                checkLogin(interceptor.checkAdmin());
            }
        } catch (BusinessException e) {
            logger.info("全局拦截异常,{}", e.toString());
            throw e;
        } catch (Throwable e) {
            logger.info("全局拦截异常,{}", e.toString());
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    private void checkLogin(Boolean checkAdmin) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_USER_TOKEN + token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }

        if (checkAdmin && !tokenUserInfoDto.getAdmin()) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }

    }

}