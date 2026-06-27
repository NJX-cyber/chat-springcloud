package com.example.model.aspect;


import com.example.model.Redis.RedisUtils;
import com.example.model.annotation.GlobalInterceptor;
import com.example.model.constants.Constants;
import com.example.model.entity.dto.TokenUserInfoDto;
import com.example.model.enums.ResponseCodeEnum;
import com.example.model.exception.BusinessException;
import com.example.model.utils.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Objects;

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

    @Before("@annotation(com.example.model.annotation.GlobalInterceptor)")
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