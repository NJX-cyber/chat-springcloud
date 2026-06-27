package com.example.agent.store;

import com.example.agent.bean.po.AIChatMessages;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * author:normal
 * date:2026/5/28 21:26
 * description:
 */
@Component
public class MongoChatMemoryStore implements ChatMemoryStore {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        AIChatMessages AIChatMessages = mongoTemplate.findOne(query, AIChatMessages.class);
        if (AIChatMessages == null) {
            return new ArrayList<>();
        }
        return ChatMessageDeserializer.messagesFromJson(AIChatMessages.getContent());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("content", ChatMessageSerializer.messagesToJson(list));
        update.set("updatedAt", new Date());
        mongoTemplate.upsert(query, update, AIChatMessages.class);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        mongoTemplate.remove(new Query(Criteria.where("memoryId").is(memoryId)), AIChatMessages.class);
    }
}