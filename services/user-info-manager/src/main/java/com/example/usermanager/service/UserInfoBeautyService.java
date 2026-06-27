package com.example.usermanager.service;

import com.example.model.entity.po.UserInfoBeauty;
import com.example.model.entity.query.UserInfoBeautyQuery;
import com.example.model.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * @Description:用户靓号表Service
 * @author:normal
 * @Date:2026/01/03 20:04:12
 */
public interface UserInfoBeautyService {

    /**
     * 根据条件查询列表
     */
    List<UserInfoBeauty> findListByParam(UserInfoBeautyQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(UserInfoBeautyQuery query);

    /**
     * 分页查询
     */
    PaginationResultVO<UserInfoBeauty> findListByPage(UserInfoBeautyQuery query);

    /**
     * 新增
     */
    Integer add(UserInfoBeauty bean);

    /**
     * 新增或修改
     */
    Integer addOrUpdate(UserInfoBeauty bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<UserInfoBeauty> listBean);

    /**
     * 批量新增或修改
     */
    Integer addOrUpdateBatch(List<UserInfoBeauty> listBean);

    /**
     * 根据Id查询
     */
    UserInfoBeauty getUserInfoBeautyById(Integer id);

    /**
     * 根据Id更新
     */
    Integer updateUserInfoBeautyById(UserInfoBeauty bean, Integer id);

    /**
     * 根据Id删除
     */
    Integer deleteUserInfoBeautyById(Integer id);

    /**
     * 根据Email查询
     */
    UserInfoBeauty getUserInfoBeautyByEmail(String email);

    /**
     * 根据Email更新
     */
    Integer updateUserInfoBeautyByEmail(UserInfoBeauty bean, String email);

    /**
     * 根据Email删除
     */
    Integer deleteUserInfoBeautyByEmail(String email);

    /**
     * 根据UserId查询
     */
    UserInfoBeauty getUserInfoBeautyByUserId(String userId);

    /**
     * 根据UserId更新
     */
    Integer updateUserInfoBeautyByUserId(UserInfoBeauty bean, String userId);

    /**
     * 根据UserId删除
     */
    Integer deleteUserInfoBeautyByUserId(String userId);

    void saveBeautyAccount(UserInfoBeauty userInfoBeauty);
}