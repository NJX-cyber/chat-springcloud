package com.example.usermanager.mapper;

import com.example.model.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Description:申请联系人
 * @author:normal
 * @Date:2026/01/06 14:52:55
 */

public interface UserContactApplyMapper<T, P> extends BaseMapper {

	/**
	 * 根据ApplyId查询
	 */
	T selectByApplyId(@Param("applyId") Integer applyId);

	/**
	 * 根据ApplyId更新
	 */
	Integer updateByApplyId(@Param("bean") T t, @Param("applyId") Integer applyId);

	/**
	 * 根据ApplyId删除
	 */
	Integer deleteByApplyId(@Param("applyId") Integer applyId);

	/**
	 * 根据ApplyUserIdAndReceiverUserIdAndContactId查询
	 */
	T selectByApplyUserIdAndReceiverUserIdAndContactId(@Param("applyUserId") String applyUserId, @Param("receiverUserId") String receiverUserId, @Param("contactId") String contactId);

	/**
	 * 根据ApplyUserIdAndReceiverUserIdAndContactId更新
	 */
	Integer updateByApplyUserIdAndReceiverUserIdAndContactId(@Param("bean") T t, @Param("applyUserId") String applyUserId, @Param("receiverUserId") String receiverUserId, @Param("contactId") String contactId);

	/**
	 * 根据ApplyUserIdAndReceiverUserIdAndContactId删除
	 */
	Integer deleteByApplyUserIdAndReceiverUserIdAndContactId(@Param("applyUserId") String applyUserId, @Param("receiverUserId") String receiverUserId, @Param("contactId") String contactId);

	/**
	 * 根据LastApplyTime查询
	 */
	T selectByLastApplyTime(@Param("lastApplyTime") Long lastApplyTime);

	/**
	 * 根据LastApplyTime更新
	 */
	Integer updateByLastApplyTime(@Param("bean") T t, @Param("lastApplyTime") Long lastApplyTime);

	/**
	 * 根据LastApplyTime删除
	 */
	Integer deleteByLastApplyTime(@Param("lastApplyTime") Long lastApplyTime);
}