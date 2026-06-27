package com.example.agent.utils;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;

import java.util.stream.Collectors;

/**
 * author:normal
 * date:2026/6/4 15:48
 * description:
 */
public class Utils {
    public static String getContent(ChatMessage msg) {
        String textContent = "";
        if (msg instanceof UserMessage userMsg) {
            textContent = userMsg.contents().stream()
                    .filter(c -> c instanceof TextContent)
                    .map(c -> ((TextContent) c).text())
                    .collect(Collectors.joining("\n"));
        } else if (msg instanceof AiMessage aiMsg) {
            textContent = aiMsg.text();
        }
        return textContent;
    }

    public static Boolean isEmpty(String str) {
        if (str == null || str.equals("") || str.equals("null") || str.equals("\u0000") || str.trim().equals("")) {
            return true;
        }
        return false;
    }
}