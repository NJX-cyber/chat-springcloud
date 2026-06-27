package com.example.agent.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * author:normal
 * date:2026/6/3 12:55
 * description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageVO{
    private String sendUserId;
    private String messageContent;
}