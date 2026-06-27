package com.example.agent.bean.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * author:normal
 * date:2026/5/28 20:49
 * description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("chat_messages")
public class AIChatMessages {
    @Id
    private String messageId;

    private String userId;

    private String content;

    @Indexed(expireAfterSeconds = 259200)
    private Date updatedAt;
}