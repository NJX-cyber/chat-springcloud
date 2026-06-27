package com.example.model.feign.fallback;

import com.example.model.entity.po.UserContact;
import com.example.model.entity.po.UserInfo;
import com.example.model.entity.query.UserContactApplyQuery;
import com.example.model.entity.query.UserContactQuery;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.feign.UserInfoFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * UserInfoFeignClient 降级工厂
 * author:normal
 * date:2026/6/15
 */
@Component
public class UserInfoFeignClientFallback implements FallbackFactory<UserInfoFeignClient> {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoFeignClientFallback.class);

    @Override
    public UserInfoFeignClient create(Throwable cause) {
        logger.error("UserInfoFeignClient 远程调用触发降级: {}", cause.getMessage(), cause);
        return new UserInfoFeignClient() {
            @Override
            public ResponseVO<UserInfo> getUserInfo(String userId) {
                logger.error("getUserInfo 降级处理, userId={}", userId);
                ResponseVO<UserInfo> response = new ResponseVO<>();
                response.setStatus("error");
                response.setCode(500);
                response.setInfo("用户信息服务暂时不可用");
                return response;
            }

            @Override
            public ResponseVO<Void> updateUserInfo(UserInfo userInfo, String userId) {
                logger.warn("updateUserInfo 降级处理, userId={}, 更新操作已跳过", userId);
                ResponseVO<Void> response = new ResponseVO<>();
                response.setStatus("success");
                response.setCode(200);
                response.setInfo("ok");
                return response;
            }

            @Override
            public Integer selectContactCount(UserContactQuery userContactQuery) {
                logger.warn("selectContactCount 降级处理, userId={}, 更新操作已跳过", userContactQuery.getUserId());
                return 0;
            }

            @Override
            public List<UserContact> selectContactList(UserContactQuery userContactQuery) {
                logger.warn("selectList 降级处理, userId={}, 更新操作已跳过", userContactQuery.getUserId());
                return null;
            }

            @Override
            public Integer findCountByParam(UserContactApplyQuery applyQuery) {
                return 0;
            }

            @Override
            public List<String> selectGroupIdList(String userId) {
                return null;
            }
        };
    }
}
