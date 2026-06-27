package com.example.agent.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT,
        streamingChatModel = "qwenStreamingChatModel",
        chatMemoryProvider = "chatMemoryProviderAgent",
        tools = "todoTaskTools",
        contentRetriever = "contentRetrieverAgent"
        )
public interface ChatAgent {
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String question);
}
