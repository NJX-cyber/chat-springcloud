package com.chat.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * @Description:
 * @author:normal
 * @Date:2026/01/26 14:34:11
 */

public interface ChatSessionUserMapper<T, P> extends BaseMapper {

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