package com.chat.service.impl;

import com.chat.constants.Constants;
import com.chat.entity.config.AppConfig;
import com.chat.entity.po.AppUpdate;
import com.chat.entity.query.SimplePage;
import com.chat.entity.query.AppUpdateQuery;
import com.chat.entity.vo.PaginationResultVO;
import com.chat.enums.AppUpdateFileTypeEnum;
import com.chat.enums.AppUpdateStatusEnum;
import com.chat.enums.PageSize;
import com.chat.enums.ResponseCodeEnum;
import com.chat.exception.BusinessException;
import com.chat.utils.StringUtils;
import org.springframework.stereotype.Service;
import com.chat.service.AppUpdateService;
import com.chat.mapper.AppUpdateMapper;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @Description:app 发布ServiceImpl
 * @author:normal
 * @Date:2026/01/24 15:01:45
 */
@Service("appUpdateService")
public class AppUpdateServiceImpl implements AppUpdateService {

    @Resource
    private AppUpdateMapper<AppUpdate, AppUpdateQuery> appUpdateMapper;

    @Resource
    private AppConfig appConfig;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<AppUpdate> findListByParam(AppUpdateQuery query) {
        return this.appUpdateMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(AppUpdateQuery query) {
        return this.appUpdateMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<AppUpdate> findListByPage(AppUpdateQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNum(), count, pageSize);
        query.setSimplePage(page);
        List<AppUpdate> list = this.findListByParam(query);
        PaginationResultVO<AppUpdate> paginationResultVO = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNum(), page.getPageTotal(), list);
        return paginationResultVO;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(AppUpdate bean) {
        return this.appUpdateMapper.insert(bean);
    }

    /**
     * 新增或修改
     */
    @Override
    public Integer addOrUpdate(AppUpdate bean) {
        return this.appUpdateMapper.insertOrUpdate(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<AppUpdate> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.appUpdateMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    @Override
    public Integer addOrUpdateBatch(List<AppUpdate> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.appUpdateMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据Id查询
     */
    @Override
    public AppUpdate getAppUpdateById(Integer id) {
        return this.appUpdateMapper.selectById(id);
    }

    /**
     * 根据Id更新
     */
    @Override
    public Integer updateAppUpdateById(AppUpdate bean, Integer id) {
        return this.appUpdateMapper.updateById(bean, id);
    }

    /**
     * 根据Id删除
     */
    @Override
    public Integer deleteAppUpdateById(Integer id) {
        AppUpdate update = this.appUpdateMapper.selectById(id);
        if (!update.getStatus().equals(AppUpdateStatusEnum.INIT.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        return this.appUpdateMapper.deleteById(id);
    }

    /**
     * 根据Version查询
     */
    @Override
    public AppUpdate getAppUpdateByVersion(String version) {
        return this.appUpdateMapper.selectByVersion(version);
    }

    /**
     * 根据Version更新
     */
    @Override
    public Integer updateAppUpdateByVersion(AppUpdate bean, String version) {
        return this.appUpdateMapper.updateByVersion(bean, version);
    }

    /**
     * 根据Version删除
     */
    @Override
    public Integer deleteAppUpdateByVersion(String version) {
        return this.appUpdateMapper.deleteByVersion(version);
    }

    @Override
    public void saveUpdate(AppUpdate appUpdate, MultipartFile file) throws IOException {
        AppUpdateFileTypeEnum fileTypeEnum = AppUpdateFileTypeEnum.getByType(appUpdate.getFileType());
        if (fileTypeEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (appUpdate.getId() != null) {
            AppUpdate update = this.appUpdateMapper.selectById(appUpdate.getId());
            if (!update.getStatus().equals(AppUpdateStatusEnum.INIT.getStatus())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        AppUpdateQuery updateQuery = new AppUpdateQuery();
        updateQuery.setOrderBy("id desc");
        updateQuery.setSimplePage(new SimplePage(0, 1));
        List<AppUpdate> updateList = this.appUpdateMapper.selectList(updateQuery);
        if (!updateList.isEmpty()) {
            AppUpdate latestAppUpdate = updateList.get(0);
            long latestVersion = Long.parseLong(latestAppUpdate.getVersion().replace(".", ""));
            long currentVersion = Long.parseLong(appUpdate.getVersion().replace(".", ""));
            if (appUpdate.getId() == null && currentVersion <= latestVersion) {
                throw new BusinessException("版本必须大于历史版本");
            }

            if (appUpdate.getId() != null && currentVersion >= latestVersion && !appUpdate.getId().equals(latestAppUpdate.getId())) {
                throw new BusinessException("版本必须大于历史版本");
            }

            AppUpdate versionDb = appUpdateMapper.selectByVersion(appUpdate.getVersion());

            if (appUpdate.getId() != null && !versionDb.getId().equals(appUpdate.getId())) {
                throw new BusinessException("版本号已存在");
            }

        }

        if (appUpdate.getId() == null) {
            appUpdate.setCreateTime(new Date());
            appUpdate.setStatus(AppUpdateStatusEnum.INIT.getStatus());
            this.appUpdateMapper.insert(appUpdate);
        } else {
            appUpdate.setStatus(null);
            appUpdate.setGrayscaleUid(null);
            this.appUpdateMapper.updateById(appUpdate, appUpdate.getId());
        }

        if (file != null) {
            File fold = new File(this.appConfig.getProjectFolder() + Constants.APP_UPDATE_FOLDER);
            if (!fold.exists()) {
                fold.mkdirs();
            }
            file.transferTo(new File(fold.getPath() + appUpdate.getId() + Constants.APP_EXE_SUFFIX));
        }
    }

    @Override
    public void postUpdate(Integer id, Integer status, String grayscaleUid) {
        AppUpdateStatusEnum statusEnum = AppUpdateStatusEnum.getByStatus(status);
        if (statusEnum == null || statusEnum.equals(AppUpdateStatusEnum.INIT)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (statusEnum.equals(AppUpdateStatusEnum.GRAYSCALE) && StringUtils.isEmpty(grayscaleUid)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (statusEnum.equals(AppUpdateStatusEnum.ALL)) {
            grayscaleUid = "";
        }

        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setStatus(status);
        appUpdate.setGrayscaleUid(grayscaleUid);
        this.appUpdateMapper.updateById(appUpdate, id);
    }

    @Override
    public AppUpdate getLatestUpdate(String appVersion, String userId) {
        return this.appUpdateMapper.selectLatestUpdate(appVersion, userId);
    }
}