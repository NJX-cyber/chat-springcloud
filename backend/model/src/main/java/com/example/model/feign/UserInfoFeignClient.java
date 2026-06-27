package com.example.model.feign;

import com.example.model.entity.po.UserContact;
import com.example.model.entity.po.UserInfo;
import com.example.model.entity.query.UserContactApplyQuery;
import com.example.model.entity.query.UserContactQuery;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.feign.fallback.UserInfoFeignClientFallback;
import com.example.model.exception.BusinessException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * user-info-manager 服务 Feign 客户端
 * author:normal
 * date:2026/6/15
 */
@FeignClient(name = "user-info-manager", path = "/feign", fallbackFactory = UserInfoFeignClientFallback.class)
public interface UserInfoFeignClient {

    /**
     * 根据userId查询用户信息
     */
    @GetMapping("/user/{userId}")
    ResponseVO<UserInfo> getUserInfo(@PathVariable("userId") String userId);

    /**
     * 根据userId更新用户信息
     */
    @PutMapping("/user/{userId}")
    ResponseVO<Void> updateUserInfo(@RequestBody UserInfo userInfo, @PathVariable("userId") String userId);

    /**
     * 根据条件查询联系人数量
     */
    @PostMapping("/user/selectContactCount")
    Integer selectContactCount(@RequestBody UserContactQuery userContactQuery);

    /**
     * 根据条件查询联系人列表
     */
    @PostMapping("/user/selectContactList")
    List<UserContact> selectContactList(@RequestBody UserContactQuery userContactQuery);

    /**
     * 根据条件查询联系人申请数量
     */
    @PostMapping("/user/selectContactApplyCount")
    Integer findCountByParam(@RequestBody UserContactApplyQuery applyQuery);

    /**
     * 根据条件查询群组列表
     */
    @GetMapping("/user/selectGroupIdList")
    List<String> selectGroupIdList(@RequestParam String userId);
}
