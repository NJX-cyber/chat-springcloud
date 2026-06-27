package com.chat.controller;

import com.chat.Redis.RedisComponent;
import com.chat.Redis.RedisUtils;
import com.chat.annotation.GlobalInterceptor;
import com.chat.constants.Constants;
import com.chat.entity.dto.MessageSendDto;
import com.chat.entity.dto.TokenUserInfoDto;
import com.chat.entity.po.UserInfo;
import com.chat.entity.vo.CheckCodeVO;
import com.chat.entity.vo.ResponseVO;
import com.chat.entity.vo.ContactInfoVO;
import com.chat.exception.BusinessException;
import com.chat.service.UserInfoService;
import com.chat.utils.CopyUtils;
import com.chat.websocket.MessageHandle;
import com.wf.captcha.ArithmeticCaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
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

    @Resource
    private MessageHandle messageHandle;

    @GetMapping("/checkCode")
    public ResponseVO checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.set(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_TIME_ONE_MIN * 2);
        CheckCodeVO checkCodeVO = new CheckCodeVO();
        checkCodeVO.setCheckCode(captcha.toBase64());
        checkCodeVO.setCheckCodeKey(checkCodeKey);
        return getSuccessResponseVO(checkCodeVO);

    }

    @Transactional(rollbackFor = Exception.class)
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

    @PostMapping("/login")
    public ResponseVO login(@NotEmpty String checkCodeKey,
                            @NotEmpty @Email String email,
                            @NotEmpty @Pattern(regexp = Constants.PATTERNS_PASSWORD) String password,
                            @NotEmpty String checkCode) {
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
            return getSuccessResponseVO(userInfoVO);
        }
        finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        }
    }

    @GlobalInterceptor
    @GetMapping("/systemSettings")
    public ResponseVO getSystemSettings() {
        return getSuccessResponseVO(this.redisComponent.getSystemSettings());
    }

    @GetMapping("/test")
    public ResponseVO test() {
        MessageSendDto dto = new MessageSendDto();
        dto.setMessageContent("测试页面" + System.currentTimeMillis());
        messageHandle.sendMessage(dto);
        return getSuccessResponseVO(null);
    }

}