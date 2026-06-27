package com.chat.service.impl;

import com.chat.entity.po.UserInfo;
import com.chat.entity.po.UserInfoBeauty;
import com.chat.entity.query.SimplePage;
import com.chat.entity.query.UserInfoBeautyQuery;
import com.chat.entity.query.UserInfoQuery;
import com.chat.entity.vo.PaginationResultVO;
import com.chat.enums.BeautyAccountStatusEnum;
import com.chat.enums.PageSize;
import com.chat.enums.ResponseCodeEnum;
import com.chat.exception.BusinessException;
import com.chat.mapper.UserInfoMapper;
import org.springframework.stereotype.Service;
import com.chat.service.UserInfoBeautyService;
import com.chat.mapper.UserInfoBeautyMapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:用户靓号表ServiceImpl
 * @author:normal
 * @Date:2026/01/03 20:04:12
 */
@Service("userInfoBeautyService")
public class UserInfoBeautyServiceImpl implements UserInfoBeautyService {

    @Resource
    private UserInfoBeautyMapper<UserInfoBeauty, UserInfoBeautyQuery> userInfoBeautyMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserInfoBeauty> findListByParam(UserInfoBeautyQuery query) {
        return this.userInfoBeautyMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(UserInfoBeautyQuery query) {
        return this.userInfoBeautyMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<UserInfoBeauty> findListByPage(UserInfoBeautyQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNum(), count, pageSize);
        query.setSimplePage(page);
        List<UserInfoBeauty> list = this.findListByParam(query);
        PaginationResultVO<UserInfoBeauty> paginationResultVO = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNum(), page.getPageTotal(), list);
        return paginationResultVO;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserInfoBeauty bean) {
        return this.userInfoBeautyMapper.insert(bean);
    }

    /**
     * 新增或修改
     */
    @Override
    public Integer addOrUpdate(UserInfoBeauty bean) {
        return this.userInfoBeautyMapper.insertOrUpdate(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserInfoBeauty> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoBeautyMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserInfoBeauty> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoBeautyMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据Id查询
     */
    @Override
    public UserInfoBeauty getUserInfoBeautyById(Integer id) {
        return this.userInfoBeautyMapper.selectById(id);
    }

    /**
     * 根据Id更新
     */
    @Override
    public Integer updateUserInfoBeautyById(UserInfoBeauty bean, Integer id) {
        return this.userInfoBeautyMapper.updateById(bean, id);
    }

    /**
     * 根据Id删除
     */
    @Override
    public Integer deleteUserInfoBeautyById(Integer id) {
        return this.userInfoBeautyMapper.deleteById(id);
    }

    /**
     * 根据Email查询
     */
    @Override
    public UserInfoBeauty getUserInfoBeautyByEmail(String email) {
        return this.userInfoBeautyMapper.selectByEmail(email);
    }

    /**
     * 根据Email更新
     */
    @Override
    public Integer updateUserInfoBeautyByEmail(UserInfoBeauty bean, String email) {
        return this.userInfoBeautyMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    @Override
    public Integer deleteUserInfoBeautyByEmail(String email) {
        return this.userInfoBeautyMapper.deleteByEmail(email);
    }

    /**
     * 根据UserId查询
     */
    @Override
    public UserInfoBeauty getUserInfoBeautyByUserId(String userId) {
        return this.userInfoBeautyMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId更新
     */
    @Override
    public Integer updateUserInfoBeautyByUserId(UserInfoBeauty bean, String userId) {
        return this.userInfoBeautyMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    @Override
    public Integer deleteUserInfoBeautyByUserId(String userId) {
        return this.userInfoBeautyMapper.deleteByUserId(userId);
    }

    @Override
    public void saveBeautyAccount(UserInfoBeauty userInfoBeauty) {
        if (userInfoBeauty == null || userInfoBeauty.getUserId() == null || userInfoBeauty.getEmail() == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (userInfoBeauty.getId() != null) {
            UserInfoBeauty dbUserInfoBeauty = this.userInfoBeautyMapper.selectById(userInfoBeauty.getId());
            if (BeautyAccountStatusEnum.USED.getStatus().equals(dbUserInfoBeauty.getStatus())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        UserInfoBeauty dbUserInfoBeauty = this.userInfoBeautyMapper.selectByEmail(userInfoBeauty.getEmail());
        // 新增时判断邮箱是否存在
        if (userInfoBeauty.getId() == null && dbUserInfoBeauty != null) {
            throw new BusinessException("靓号邮箱已存在");
        }

        // 修改时判断邮箱是否存在
        if (userInfoBeauty.getId() != null && dbUserInfoBeauty != null
                && !dbUserInfoBeauty.getId().equals(userInfoBeauty.getId())) {
            throw new BusinessException("靓号邮箱重复");
        }

        // 判断靓号是否存在
        // 新增
        dbUserInfoBeauty = this.userInfoBeautyMapper.selectByUserId(userInfoBeauty.getUserId());
        if (userInfoBeauty.getId() == null && dbUserInfoBeauty != null) {
            throw new BusinessException("靓号已存在");
        }
        // 修改
        if (userInfoBeauty.getId() != null && dbUserInfoBeauty != null
                && !dbUserInfoBeauty.getId().equals(userInfoBeauty.getId())) {
            throw new BusinessException("靓号重复");
        }

        // 判断邮箱是否已经注册
        UserInfo userInfo = this.userInfoMapper.selectByEmail(userInfoBeauty.getEmail());
        if (userInfo != null) {
            throw new BusinessException("邮箱已被注册");
        }
        userInfo = this.userInfoMapper.selectByUserId(userInfoBeauty.getUserId());
        if (userInfo != null) {
            throw new BusinessException("靓号已被注册");
        }

        if (userInfoBeauty.getId() == null) {
            userInfoBeauty.setStatus(BeautyAccountStatusEnum.NO_USE.getStatus());
            this.userInfoBeautyMapper.insert(userInfoBeauty);
        } else {
            this.userInfoBeautyMapper.updateById(userInfoBeauty,userInfoBeauty.getId());
        }
    }
}