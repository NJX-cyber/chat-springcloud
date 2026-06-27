package com.example.usermanager.controller;

import com.example.model.annotation.GlobalInterceptor;
import com.example.model.annotation.RateLimit;
import com.example.model.entity.po.GroupInfo;
import com.example.model.entity.query.GroupInfoQuery;
import com.example.model.entity.vo.PaginationResultVO;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.enums.GroupStatusEnum;
import com.example.model.enums.ResponseCodeEnum;
import com.example.model.controller.BaseController;
import com.example.model.exception.BusinessException;
import com.example.usermanager.service.GroupInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * author:normal
 * date:2026/1/22 20:39
 * description:
 */

@RestController("adminGroupController")
@RequestMapping("/admin")
@Validated
public class AdminGroupController extends BaseController {

    @Resource
    private GroupInfoService groupInfoService;

    @RateLimit
    @GetMapping("/loadGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadGroup(GroupInfoQuery groupInfoQuery) {
        groupInfoQuery.setOrderBy("create_time desc");
        groupInfoQuery.setQueryMemberCount(true);
        groupInfoQuery.setQueryOwnerName(true);
        PaginationResultVO<GroupInfo> listByPage = this.groupInfoService.findListByPage(groupInfoQuery);
        return getSuccessResponseVO(listByPage);
    }

    @RateLimit
    @PutMapping("/dissolutionGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO dissolutionGroup(@NotEmpty String groupId,
                                       @NotNull Integer status) {
        GroupStatusEnum statusEnum = GroupStatusEnum.getByStatus(status);
        if (statusEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (statusEnum.equals(GroupStatusEnum.DISSOLUTION)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        GroupInfo groupInfo = this.groupInfoService.getGroupInfoByGroupId(groupId);
        if (groupInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        this.groupInfoService.dissolutionGroup(groupInfo.getGroupOwnerId(), groupId, status);
        return getSuccessResponseVO(null);
    }
}