package com.example.model.aspect;

import com.example.model.Redis.RedisUtils;
import com.example.model.annotation.RateLimit;
import com.example.model.constants.Constants;
import com.example.model.entity.dto.TokenUserInfoDto;
import com.example.model.enums.ResponseCodeEnum;
import com.example.model.exception.BusinessException;
import com.example.model.utils.IpUtils;
import com.example.model.utils.StringUtils;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @RateLimit 注解切面 — 针对指定用户做限流。
 * 每个用户独立计数，限流策略复用 application.yml 中配置的实例。
 *
 * @author normal
 * @date 2026/6/17
 */
@Aspect
@Component("rateLimitAspect")
public class RateLimitAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);

    @Resource
    private RateLimiterRegistry rateLimiterRegistry;

    @Resource
    private RedisUtils<Object> redisUtils;

    /** 按用户 ID 缓存 RateLimiter 实例 */
    private final ConcurrentHashMap<String, RateLimiter> userLimiters = new ConcurrentHashMap<>();

    @Before("@annotation(rateLimit)")
    public void checkRateLimit(JoinPoint point, RateLimit rateLimit) {
        String key = extractUserId();
        if (key == null) {
            // 未登录用户用 IP 作为限流维度，避免所有匿名用户共享一个池子
            key = "ip:" + IpUtils.getClientIp();
        }

        RateLimiter limiter = userLimiters.computeIfAbsent(key,
                uid -> buildLimiter(rateLimit.name(), uid));

        boolean allowed = limiter.acquirePermission();
        if (!allowed) {
            logger.warn("限流触发, key={}, instance={}",
                    key, rateLimit.name());
            throw new BusinessException(ResponseCodeEnum.CODE_429);
        }
    }


    /**
     * 从请求 token 中提取用户 ID
     */
    private String extractUserId() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes)
                    Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            String token = request.getHeader("token");
            if (StringUtils.isEmpty(token)) {
                logger.debug("限流切面: 请求无 token，使用匿名用户");
                return null;
            }
            TokenUserInfoDto userInfo = (TokenUserInfoDto) redisUtils.get(
                    Constants.REDIS_KEY_WS_USER_TOKEN + token);
            if (userInfo == null) {
                logger.debug("限流切面: token 无效，使用匿名用户");
                return null;
            }
            return userInfo.getUserId();
        } catch (Exception e) {
            logger.debug("限流切面提取用户ID异常，使用匿名用户: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 用配置文件中指定实例的策略，为指定用户创建 RateLimiter
     */
    private RateLimiter buildLimiter(String configName, String userId) {
        RateLimiterConfig config = rateLimiterRegistry.getConfiguration(configName)
                .orElseGet(() -> {
                    logger.warn("未找到 rateLimiter 配置实例 '{}', 使用默认配置", configName);
                    return rateLimiterRegistry.getDefaultConfig();
                });
        logger.info("创建用户 RateLimiter: userId={}, configName={}, limitForPeriod={}, refreshPeriod={}ms",
                userId, configName,
                config.getLimitForPeriod(),
                config.getLimitRefreshPeriod().toMillis());
        return RateLimiter.of(configName + ":" + userId, config);
    }
}
