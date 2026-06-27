package com.example.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.model.entity.po.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LocalHistoryMapper extends BaseMapper<ChatMessage> {
}
