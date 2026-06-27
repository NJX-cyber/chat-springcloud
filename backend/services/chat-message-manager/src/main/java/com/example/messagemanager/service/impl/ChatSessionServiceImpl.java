package com.example.messagemanager.service.impl;

import com.example.messagemanager.mapper.ChatSessionMapper;
import com.example.messagemanager.service.ChatSessionService;
import com.example.model.annotation.CircuitBreak;
import com.example.model.entity.po.ChatSession;
import com.example.model.entity.query.ChatSessionQuery;
import com.example.model.entity.query.SimplePage;
import com.example.model.entity.vo.PaginationResultVO;
import com.example.model.enums.PageSize;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:ServiceImpl
 * @author:normal
 * @Date:2026/01/26 14:34:11
 */
@Service("chatSessionService")
public class ChatSessionServiceImpl implements ChatSessionService {

	@Resource
	private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ChatSession> findListByParam(ChatSessionQuery query){
		return this.chatSessionMapper.selectList(query);
	}

	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(ChatSessionQuery query) {
		return this.chatSessionMapper.selectCount(query);
	}

	/**
	 * 分页查询
	 */
	@CircuitBreak
	@Override
	public PaginationResultVO<ChatSession> findListByPage(ChatSessionQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNum(), count, pageSize);
		query.setSimplePage(page);
		List<ChatSession> list = this.findListByParam(query);
		PaginationResultVO<ChatSession> paginationResultVO = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNum(), page.getPageTotal(), list);
		return paginationResultVO;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ChatSession bean) {
		return this.chatSessionMapper.insert(bean);
	}

	/**
	 * 新增或修改
	 */
	@Override
	public Integer addOrUpdate(ChatSession bean) {
		return this.chatSessionMapper.insertOrUpdate(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ChatSession> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatSessionMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ChatSession> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatSessionMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据SessionId查询
	 */
	@Override
	public ChatSession getChatSessionBySessionId(String sessionId) {
		return this.chatSessionMapper.selectBySessionId(sessionId);
	}

	/**
	 * 根据SessionId更新
	 */
	@Override
	public Integer updateChatSessionBySessionId(ChatSession bean, String sessionId) {
		return this.chatSessionMapper.updateBySessionId(bean, sessionId);
	}

	/**
	 * 根据SessionId删除
	 */
	@Override
	public Integer deleteChatSessionBySessionId(String sessionId) {
		return this.chatSessionMapper.deleteBySessionId(sessionId);
	}
}