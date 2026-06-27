package com.example.usermanager.mapper;

import com.example.model.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Description:群组
 * @author:normal
 * @Date:2026/01/06 14:52:55
 */

public interface GroupInfoMapper<T, P> extends BaseMapper {

	/**
	 * 根据GroupId查询
	 */
	T selectByGroupId(@Param("groupId") String groupId);

	/**
	 * 根据GroupId更新
	 */
	Integer updateByGroupId(@Param("bean") T t, @Param("groupId") String groupId);

	/**
	 * 根据GroupId删除
	 */
	Integer deleteByGroupId(@Param("groupId") String groupId);
}