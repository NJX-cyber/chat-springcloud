package com.example.usermanager.service;


import com.example.model.entity.po.AppUpdate;
import com.example.model.entity.query.AppUpdateQuery;
import com.example.model.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @Description:app 发布Service
 * @author:normal
 * @Date:2026/01/24 15:01:45
 */
public interface AppUpdateService {

	/**
	 * 根据条件查询列表
	 */
	List<AppUpdate> findListByParam(AppUpdateQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(AppUpdateQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<AppUpdate> findListByPage(AppUpdateQuery query);

	/**
	 * 新增
	 */
	Integer add(AppUpdate bean);

	/**
	 * 新增或修改
	 */
	Integer addOrUpdate(AppUpdate bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<AppUpdate> listBean);

	/**
	 * 批量新增或修改
	 */
	Integer addOrUpdateBatch(List<AppUpdate> listBean);

	/**
	 * 根据Id查询
	 */
	AppUpdate getAppUpdateById(Integer id);

	/**
	 * 根据Id更新
	 */
	Integer updateAppUpdateById(AppUpdate bean, Integer id);

	/**
	 * 根据Id删除
	 */
	Integer deleteAppUpdateById(Integer id);

	/**
	 * 根据Version查询
	 */
	AppUpdate getAppUpdateByVersion(String version);

	/**
	 * 根据Version更新
	 */
	Integer updateAppUpdateByVersion(AppUpdate bean, String version);

	/**
	 * 根据Version删除
	 */
	Integer deleteAppUpdateByVersion(String version);

	void saveUpdate(AppUpdate appUpdate, MultipartFile file) throws IOException;

    void postUpdate(Integer id, Integer status, String grayscaleUid);

    AppUpdate getLatestUpdate(String appVersion, String uid);
}