package com.example.model.aspect;

import com.example.model.Redis.RedisUtils;
import com.example.model.annotation.CircuitBreak;
import com.example.model.constants.Constants;
import com.example.model.entity.dto.TokenUserInfoDto;
import com.example.model.enums.ResponseCodeEnum;
import com.example.model.exception.BusinessException;
import com.example.model.utils.StringUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @CircuitBreak 注解切面 — 每个用户独立的熔断器。
 * 跟踪用户的请求成功率，超过阈值则熔断，冷却后自动恢复。
 *
 * @author normal
 * @date 2026/6/17
 */
@Aspect
@Component("circuitBreakAspect")
public class CircuitBreakAspect {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakAspect.class);

    @Resource
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Resource
    private RedisUtils<Object> redisUtils;

    /** 按用户 ID 缓存 CircuitBreaker 实例 */
    private final ConcurrentHashMap<String, CircuitBreaker> userBreakers = new ConcurrentHashMap<>();

    @Around("@annotation(circuitBreak)")
    public Object around(ProceedingJoinPoint point, CircuitBreak circuitBreak) throws Throwable {
        String userId = extractUserId();
        if (userId == null) {
            userId = "anonymous";
        }

        CircuitBreaker breaker = userBreakers.computeIfAbsent(userId,
                uid -> buildBreaker(circuitBreak.name(), uid));

        if (!breaker.tryAcquirePermission()) {
            logger.warn("用户熔断触发, userId={}, state={}", userId, breaker.getState());
            throw new BusinessException(ResponseCodeEnum.CODE_429);
        }

        long start = System.nanoTime();
        try {
            Object result = point.proceed();
            breaker.onSuccess(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            return result;
        } catch (Throwable t) {
            breaker.onError(System.nanoTime() - start, TimeUnit.NANOSECONDS, t);
            throw t;
        }
    }

    private String extractUserId() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes)
                    Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            String token = request.getHeader("token");
            if (StringUtils.isEmpty(token)) {
                logger.debug("熔断切面: 请求无 token，使用匿名用户");
                return null;
            }
            TokenUserInfoDto userInfo = (TokenUserInfoDto) redisUtils.get(
                    Constants.REDIS_KEY_WS_USER_TOKEN + token);
            if (userInfo == null) {
                logger.debug("熔断切面: token 无效，使用匿名用户");
                return null;
            }
            return userInfo.getUserId();
        } catch (Exception e) {
            logger.debug("熔断切面提取用户ID异常，使用匿名用户: {}", e.getMessage());
            return null;
        }
    }

    private CircuitBreaker buildBreaker(String configName, String userId) {
        CircuitBreakerConfig config = circuitBreakerRegistry.getConfiguration(configName)
                .orElseGet(circuitBreakerRegistry::getDefaultConfig);
        logger.debug("为 userId={} 创建 CircuitBreaker, config={}", userId, configName);
        return CircuitBreaker.of(configName + ":" + userId, config);
    }
}
