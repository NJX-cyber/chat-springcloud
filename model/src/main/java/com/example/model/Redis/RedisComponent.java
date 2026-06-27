package com.example.model.Redis;

import com.example.model.constants.Constants;
import com.example.model.entity.dto.SystemSettingsDto;
import com.example.model.entity.dto.TokenUserInfoDto;
import com.example.model.utils.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * author:normal
 * date:2026/1/5 15:13
 * description:
 */
@Component("redisComponent")
public class RedisComponent {

    @Resource
    private RedisUtils redisUtils;

    /**
     * 获取心跳
     *
     * @param userId
     * @return
     */
    public Long getUserHeartBeat(String userId) {
        return (Long) this.redisUtils.get(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }

    public void saveUserHeartBeat(String userId) {
        this.redisUtils.set(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId, System.currentTimeMillis(), Constants.REDIS_KEY_EXPIRES_HEART_BEAT);
    }

    public void removeUserHeartBeat(String userId) {
        this.redisUtils.delete(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }

    /**
     * 保存用户信息
     *
     * @param tokenUserInfoDto
     */
    public void saveTokenUserInfoDto(TokenUserInfoDto tokenUserInfoDto) {
        this.redisUtils.set(Constants.REDIS_KEY_WS_USER_TOKEN + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_TIME_ONE_DAY);
        this.redisUtils.set(Constants.REDIS_KEY_WS_USER_ID + tokenUserInfoDto.getUserId(), tokenUserInfoDto.getToken(), Constants.REDIS_TIME_ONE_DAY);
    }

    public TokenUserInfoDto getTokenUserInfoDto(String token) {
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) this.redisUtils.get(Constants.REDIS_KEY_WS_USER_TOKEN + token);
        return tokenUserInfoDto;
    }

    public TokenUserInfoDto getTokenUserInfoDtoByUserId(String userId) {
        String token = (String) redisUtils.get(Constants.REDIS_KEY_WS_USER_ID + userId);
        return getTokenUserInfoDto(token);
    }

    public void cleanUserTokenByUserId(String userId) {
        String token = (String) redisUtils.get(Constants.REDIS_KEY_WS_USER_ID + userId);
        if (StringUtils.isEmpty(token)) {
            return;
        }
        redisUtils.delete(Constants.REDIS_KEY_WS_USER_ID + userId);
        redisUtils.delete(Constants.REDIS_KEY_WS_USER_TOKEN + token);
    }

    public SystemSettingsDto getSystemSettings() {
        SystemSettingsDto systemSettingsDto = (SystemSettingsDto) this.redisUtils.get(Constants.REDIS_KEY_SYSTEM_SETTINGS);
        if (systemSettingsDto == null) {
            systemSettingsDto = new SystemSettingsDto();
        }
        return systemSettingsDto;
    }

    public void saveSystemSettings(SystemSettingsDto systemSettingsDto) {
        this.redisUtils.set(Constants.REDIS_KEY_SYSTEM_SETTINGS, systemSettingsDto);
    }

    public void saveUserContactBatch(String userId, List<String> userContactList) {
        this.redisUtils.lPushAll(Constants.REDIS_KEY_WS_USER_CONTACT + userId, userContactList, Constants.REDIS_TIME_ONE_DAY);
    }

    public void saveUserContact(String userId, String contactId) {
        List<String> contactList = getUserContactList(userId);
        if (contactList.contains(contactId)) {
            return;
        }
        this.redisUtils.lPush(Constants.REDIS_KEY_WS_USER_CONTACT + userId, contactId, Constants.REDIS_TIME_ONE_DAY);
    }

    public List<String> getUserContactList(String userId) {
        return (List<String>) this.redisUtils.getQueueList(Constants.REDIS_KEY_WS_USER_CONTACT + userId);

    }

    public void cleanUserContactBatch(String userId) {
        this.redisUtils.delete(Constants.REDIS_KEY_WS_USER_CONTACT + userId);
    }

    public void removeUserContact(String userId, String contactId) {
        this.redisUtils.remove(Constants.REDIS_KEY_WS_USER_CONTACT + userId, contactId);
    }

}