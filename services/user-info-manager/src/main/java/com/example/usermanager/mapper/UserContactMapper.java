package com.example.usermanager.mapper;

import com.example.model.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Description:联系人
 * @author:normal
 * @Date:2026/01/06 14:52:55
 */

public interface UserContactMapper<T, P> extends BaseMapper {

	/**
	 * 根据UserIdAndContactId查询
	 */
	T selectByUserIdAndContactId(@Param("userId") String userId, @Param("contactId") String contactId);

	/**
	 * 根据UserIdAndContactId更新
	 */
	Integer updateByUserIdAndContactId(@Param("bean") T t, @Param("userId") String userId, @Param("contactId") String contactId);

	/**
	 * 根据UserIdAndContactId删除
	 */
	Integer deleteByUserIdAndContactId(@Param("userId") String userId, @Param("contactId") String contactId);
}