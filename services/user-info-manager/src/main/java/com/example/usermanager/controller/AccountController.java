package com.example.usermanager.controller;


import com.example.model.Redis.RedisComponent;
import com.example.model.Redis.RedisUtils;
import com.example.model.annotation.GlobalInterceptor;
import com.example.model.annotation.RateLimit;
import com.example.model.constants.Constants;
import com.example.model.entity.dto.TokenUserInfoDto;
import com.example.model.entity.po.UserInfo;
import com.example.model.entity.vo.CheckCodeVO;
import com.example.model.entity.vo.ContactInfoVO;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.controller.BaseController;
import com.example.model.exception.BusinessException;
import com.example.model.utils.CopyUtils;
import com.example.model.utils.IpUtils;
import com.example.usermanager.service.UserInfoService;
import com.example.usermanager.utils.ArithCaptcha;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * author:normal
 * date:2026/1/3 21:27
 * description:
 */
@RestController("countController")
@RequestMapping("/account")
@Validated
public class AccountController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    @RateLimiter(name = "lim-without-login")
    @GetMapping("/checkCode")
    public ResponseVO checkCode() {
        // IP 频控: 同一 IP 两次获取验证码间隔 ≥ 1s
        String clientIp = IpUtils.getClientIp();
        String ipKey = Constants.REDIS_KEY_CAPTCHA_IP + clientIp;
        Object lastRequest = redisUtils.get(ipKey);
        if (lastRequest != null) {
            throw new BusinessException("操作过于频繁，请稍后再试");
        }
        redisUtils.set(ipKey, "1", Constants.CAPTCHA_IP_INTERVAL);

        // 简单四则运算验证码（仅加减，单位数，纯JDK无外部依赖）
        ArithCaptcha captcha = ArithCaptcha.create();
        String code = captcha.getCode();
        String base64 = captcha.getBase64();
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.set(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_TIME_ONE_MIN * 2);
        CheckCodeVO checkCodeVO = new CheckCodeVO();
        checkCodeVO.setCheckCode(base64);
        checkCodeVO.setCheckCodeKey(checkCodeKey);
        return getSuccessResponseVO(checkCodeVO);

    }

    @RateLimit
    @Transactional(rollbackFor = Exception.class)
    @RateLimiter(name = "lim-without-login")
    @PostMapping("/register")
    public ResponseVO register(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String email,
                               @NotEmpty String password,
                               @NotEmpty String nickname,
                               @NotEmpty String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase(redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey).toString())) {
                throw new BusinessException("验证码错误");
            }
            userInfoService.register(email, password, nickname);
            return getSuccessResponseVO(null);
        } finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        }
    }

    @RateLimiter(name = "lim-without-login")
    @PostMapping("/login")
    public ResponseVO login(@NotEmpty String checkCodeKey,
                            @NotEmpty @Email String email,
                            @NotEmpty @Pattern(regexp = Constants.PATTERNS_PASSWORD) String password,
                            @NotEmpty String checkCode) {
        // IP 锁定检查
        String clientIp = IpUtils.getClientIp();
        String lockKey = Constants.REDIS_KEY_LOGIN_LOCK + clientIp;
        if (redisUtils.get(lockKey) != null) {
            throw new BusinessException("登录尝试过于频繁，请10分钟后再试");
        }

        String redisKey = Constants.REDIS_KEY_CHECK_CODE + checkCodeKey;
        Object cacheCodeObj = redisUtils.get(redisKey);
        if (cacheCodeObj == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        String cacheCode = cacheCodeObj.toString();
        if (!checkCode.equalsIgnoreCase(cacheCode)) {
            throw new BusinessException("验证码输入错误");
        }
        try {
            TokenUserInfoDto tokenUserInfoDto = this.userInfoService.login(email, password);
            UserInfo userInfo = this.userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
            ContactInfoVO userInfoVO = CopyUtils.copy(userInfo, ContactInfoVO.class);
            userInfoVO.setToken(tokenUserInfoDto.getToken());
            userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
            // 登录成功，清除失败计数
            redisUtils.delete(Constants.REDIS_KEY_LOGIN_FAIL + clientIp);
            return getSuccessResponseVO(userInfoVO);
        } catch (BusinessException e) {
            // 登录失败：累计 IP 失败次数
            trackLoginFail(clientIp);
            throw e;
        } finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        }
    }

    /**
     * 记录 IP 登录失败次数，达到阈值后锁定 10 分钟
     */
    private void trackLoginFail(String clientIp) {
        String failKey = Constants.REDIS_KEY_LOGIN_FAIL + clientIp;
        Long count = redisUtils.increment(failKey, 1);
        if (count == null) {
            count = 1L;
        }
        redisUtils.expire(failKey, Constants.REDIS_TIME_TEN_MIN);
        if (count >= Constants.MAX_LOGIN_FAIL_COUNT) {
            redisUtils.set(Constants.REDIS_KEY_LOGIN_LOCK + clientIp, "locked", Constants.LOGIN_LOCK_DURATION);
            logger.warn("IP {} 连续登录失败{}次，锁定{}秒", clientIp, count, Constants.LOGIN_LOCK_DURATION);
        }
    }

    @GlobalInterceptor
    @RateLimit
    @GetMapping("/systemSettings")
    public ResponseVO getSystemSettings() {
        return getSuccessResponseVO(this.redisComponent.getSystemSettings());
    }


}