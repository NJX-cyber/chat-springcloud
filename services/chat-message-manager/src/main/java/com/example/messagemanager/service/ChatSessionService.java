package com.example.messagemanager.service;

import com.example.model.entity.po.ChatSession;
import com.example.model.entity.query.ChatSessionQuery;
import com.example.model.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * @Description:Service
 * @author:normal
 * @Date:2026/01/26 14:34:11
 */
public interface ChatSessionService {

	/**
	 * 根据条件查询列表
	 */
	List<ChatSession> findListByParam(ChatSessionQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(ChatSessionQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ChatSession> findListByPage(ChatSessionQuery query);

	/**
	 * 新增
	 */
	Integer add(ChatSession bean);

	/**
	 * 新增或修改
	 */
	Integer addOrUpdate(ChatSession bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<ChatSession> listBean);

	/**
	 * 批量新增或修改
	 */
	Integer addOrUpdateBatch(List<ChatSession> listBean);

	/**
	 * 根据SessionId查询
	 */
	ChatSession getChatSessionBySessionId(String sessionId);

	/**
	 * 根据SessionId更新
	 */
	Integer updateChatSessionBySessionId(ChatSession bean, String sessionId);

	/**
	 * 根据SessionId删除
	 */
	Integer deleteChatSessionBySessionId(String sessionId);
}