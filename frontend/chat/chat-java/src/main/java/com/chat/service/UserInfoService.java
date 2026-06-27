package com.chat.service;

import com.chat.entity.dto.TokenUserInfoDto;
import com.chat.entity.po.UserInfo;
import com.chat.entity.query.UserInfoQuery;
import com.chat.entity.vo.PaginationResultVO;
import com.chat.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @Description:用户信息表Service
 * @author:normal
 * @Date:2026/01/03 20:04:12
 */
public interface UserInfoService {

    /**
     * 根据条件查询列表
     */
    List<UserInfo> findListByParam(UserInfoQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(UserInfoQuery query);

    /**
     * 分页查询
     */
    PaginationResultVO<UserInfo> findListByPage(UserInfoQuery query);

    /**
     * 新增
     */
    Integer add(UserInfo bean);

    /**
     * 新增或修改
     */
    Integer addOrUpdate(UserInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<UserInfo> listBean);

    /**
     * 批量新增或修改
     */
    Integer addOrUpdateBatch(List<UserInfo> listBean);

    /**
     * 根据UserId查询
     */
    UserInfo getUserInfoByUserId(String userId);

    /**
     * 根据UserId更新
     */
    Integer updateUserInfoByUserId(UserInfo bean, String userId);

    /**
     * 根据UserId删除
     */
    Integer deleteUserInfoByUserId(String userId);

    /**
     * 根据Email查询
     */
    UserInfo getUserInfoByEmail(String email);

    /**
     * 根据Email更新
     */
    Integer updateUserInfoByEmail(UserInfo bean, String email);

    /**
     * 根据Email删除
     */
    Integer deleteUserInfoByEmail(String email);

    /**
     * 注册
     */
    void register(String email, String password, String nickname);

    /**
     * 登录
     */
    TokenUserInfoDto login(String email, String password);

    void updateUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException;

    void updateUserStatus(Integer status, String userId);

    void forceOffLine(String userId);

}