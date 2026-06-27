package com.chat.controller;

import com.chat.annotation.GlobalInterceptor;
import com.chat.entity.po.GroupInfo;
import com.chat.entity.query.GroupInfoQuery;
import com.chat.entity.vo.PaginationResultVO;
import com.chat.entity.vo.ResponseVO;
import com.chat.enums.GroupStatusEnum;
import com.chat.enums.ResponseCodeEnum;
import com.chat.exception.BusinessException;
import com.chat.service.GroupInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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

    @GetMapping("/loadGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadGroup(GroupInfoQuery groupInfoQuery) {
        groupInfoQuery.setOrderBy("create_time desc");
        groupInfoQuery.setQueryMemberCount(true);
        groupInfoQuery.setQueryOwnerName(true);
        PaginationResultVO<GroupInfo> listByPage = this.groupInfoService.findListByPage(groupInfoQuery);
        return getSuccessResponseVO(listByPage);
    }

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