package com.example.messagemanager.mapper;

import com.example.model.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Description:
 * @author:normal
 * @Date:2026/01/26 14:34:11
 */

public interface ChatMessageMapper<T, P> extends BaseMapper {

	/**
	 * 根据MessageId查询
	 */
	T selectByMessageId(@Param("messageId") Long messageId);

	/**
	 * 根据MessageId更新
	 */
	Integer updateByMessageId(@Param("bean") T t, @Param("messageId") Long messageId);

	/**
	 * 根据MessageId删除
	 */
	Integer deleteByMessageId(@Param("messageId") Long messageId);
}