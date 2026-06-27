package com.example.messagemanager.mapper;

import com.example.model.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Description:
 * @author:normal
 * @Date:2026/01/26 14:34:11
 */

public interface ChatSessionMapper<T, P> extends BaseMapper {

	/**
	 * 根据SessionId查询
	 */
	T selectBySessionId(@Param("sessionId") String sessionId);

	/**
	 * 根据SessionId更新
	 */
	Integer updateBySessionId(@Param("bean") T t, @Param("sessionId") String sessionId);

	/**
	 * 根据SessionId删除
	 */
	Integer deleteBySessionId(@Param("sessionId") String sessionId);
}