package com.example.usermanager.controller;

import com.example.model.entity.po.UserContact;
import com.example.model.entity.po.UserInfo;
import com.example.model.entity.query.UserContactApplyQuery;
import com.example.model.entity.query.UserContactQuery;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.enums.ResponseCodeEnum;
import com.example.model.enums.UserContactStatusEnum;
import com.example.model.enums.UserContactTypeEnum;
import com.example.model.controller.BaseController;
import com.example.model.exception.BusinessException;
import com.example.model.utils.StringUtils;
import com.example.usermanager.service.UserContactApplyService;
import com.example.usermanager.service.UserContactService;
import com.example.usermanager.service.UserInfoService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 供内部 Feign 调用的用户信息端点
 * author:normal
 * date:2026/6/15
 */
@RestController
@RequestMapping("/feign")
public class FeignUserController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(FeignUserController.class);

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserContactApplyService userContactApplyService;

    /**
     * 根据userId查询用户信息（内部调用，无需token）
     * @return
     */
    @GetMapping("/user/{userId}")
    public ResponseVO getUserInfo(@PathVariable String userId) {
        logger.debug("Feign 调用: getUserInfo, userId={}", userId);
        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        return getSuccessResponseVO(userInfo);
    }

    /**
     * 根据userId更新用户信息（内部调用，无需token，支持部分字段更新）
     * @return
     */
    @PutMapping("/user/{userId}")
    public ResponseVO updateUserInfo(@RequestBody UserInfo userInfo,
                                     @PathVariable String userId) {
        logger.debug("Feign 调用: updateUserInfo, userId={}", userId);
        userInfoService.updateUserInfoByUserId(userInfo, userId);
        return getSuccessResponseVO(null);
    }
    /**
     * 根据条件查询联系人数量
     */
    @PostMapping("/user/selectContactCount")
    public ResponseVO selectContactCount(@RequestBody UserContactQuery userContactQuery) {
        logger.debug("Feign 调用: selectContactCount, userId={}, userContactId={}", userContactQuery.getUserId(), userContactQuery.getContactId());
        Integer count = this.userContactService.findCountByParam(userContactQuery);
        return getSuccessResponseVO(count);
    }

    /**
     * 根据条件查询联系人列表
     */

    @PostMapping("/user/selectContactList")
    List<UserContact> selectContactList(@RequestBody UserContactQuery userContactQuery){
        return this.userContactService.findListByParam(userContactQuery);
    }

    /**
     * 根据条件查询联系人申请数量
     */
    @PostMapping("/user/selectContactApplyCount")
    Integer findCountByParam(@RequestBody UserContactApplyQuery applyQuery){
        return this.userContactApplyService.findCountByParam(applyQuery);
    }
    /**
     * 根据条件查询群组列表
     */
    @GetMapping("/user/selectGroupIdList")
    List<String> selectGroupIdList(@RequestParam String userId){
        if (StringUtils.isEmpty(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
        UserContactQuery query = new UserContactQuery();
        query.setUserId(userId);
        query.setContactType(UserContactTypeEnum.GROUP.getType());
        query.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        return this.userContactService.findListByParam(query).stream().map(UserContact::getContactId).collect(Collectors.toList());
    }
}
