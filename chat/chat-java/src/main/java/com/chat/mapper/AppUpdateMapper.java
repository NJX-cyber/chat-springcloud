package com.chat.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * @Description:app 发布
 * @author:normal
 * @Date:2026/01/24 15:01:45
 */

public interface AppUpdateMapper<T, P> extends BaseMapper {

	/**
	 * 根据Id查询
	 */
	T selectById(@Param("id") Integer id);

	/**
	 * 根据Id更新
	 */
	Integer updateById(@Param("bean") T t, @Param("id") Integer id);

	/**
	 * 根据Id删除
	 */
	Integer deleteById(@Param("id") Integer id);

	/**
	 * 根据Version查询
	 */
	T selectByVersion(@Param("version") String version);

	/**
	 * 根据Version更新
	 */
	Integer updateByVersion(@Param("bean") T t, @Param("version") String version);

	/**
	 * 根据Version删除
	 */
	Integer deleteByVersion(@Param("version") String version);

	T selectLatestUpdate(String appVersion, String userId);
}