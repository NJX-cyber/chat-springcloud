package com.chat.service;

import com.chat.entity.dto.TokenUserInfoDto;
import com.chat.entity.po.UserContactApply;
import com.chat.entity.query.UserContactApplyQuery;
import com.chat.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * @Description:申请联系人Service
 * @author:normal
 * @Date:2026/01/06 14:52:55
 */
public interface UserContactApplyService {

	/**
	 * 根据条件查询列表
	 */
	List<UserContactApply> findListByParam(UserContactApplyQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(UserContactApplyQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserContactApply> findListByPage(UserContactApplyQuery query);

	/**
	 * 新增
	 */
	Integer add(UserContactApply bean);

	/**
	 * 新增或修改
	 */
	Integer addOrUpdate(UserContactApply bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserContactApply> listBean);

	/**
	 * 批量新增或修改
	 */
	Integer addOrUpdateBatch(List<UserContactApply> listBean);

	/**
	 * 根据ApplyId查询
	 */
	UserContactApply getUserContactApplyByApplyId(Integer applyId);

	/**
	 * 根据ApplyId更新
	 */
	Integer updateUserContactApplyByApplyId(UserContactApply bean, Integer applyId);

	/**
	 * 根据ApplyId删除
	 */
	Integer deleteUserContactApplyByApplyId(Integer applyId);

	/**
	 * 根据ApplyUserIdAndReceiverUserIdAndContactId查询
	 */
	UserContactApply getUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId(String applyUserId, String receiverUserId, String contactId);

	/**
	 * 根据ApplyUserIdAndReceiverUserIdAndContactId更新
	 */
	Integer updateUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId(UserContactApply bean, String applyUserId, String receiverUserId, String contactId);

	/**
	 * 根据ApplyUserIdAndReceiverUserIdAndContactId删除
	 */
	Integer deleteUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId(String applyUserId, String receiverUserId, String contactId);

	/**
	 * 根据LastApplyTime查询
	 */
	UserContactApply getUserContactApplyByLastApplyTime(Long lastApplyTime);

	/**
	 * 根据LastApplyTime更新
	 */
	Integer updateUserContactApplyByLastApplyTime(UserContactApply bean, Long lastApplyTime);

	/**
	 * 根据LastApplyTime删除
	 */
	Integer deleteUserContactApplyByLastApplyTime(Long lastApplyTime);

	void dealWithApply(String userId, Integer applyId, Integer status);

	Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo);
}