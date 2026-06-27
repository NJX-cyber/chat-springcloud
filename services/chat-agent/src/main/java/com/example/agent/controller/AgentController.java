package com.example.agent.controller;

import com.example.agent.assistant.ChatAgent;
import com.example.agent.component.RefinedResponseHolder;
import com.example.agent.service.ChatMessageService;
import com.example.model.constants.Constants;
import com.example.model.entity.po.ChatSession;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.annotation.CircuitBreak;
import com.example.model.annotation.GlobalInterceptor;
import com.example.model.annotation.RateLimit;
import com.example.model.entity.dto.ChatForm;
import com.example.model.enums.ResponseCodeEnum;
import com.example.model.controller.BaseController;
import com.example.model.exception.BusinessException;
import com.example.model.feign.ChatMessageFeignClient;
import com.example.model.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * author:normal
 * date:2026/5/29 11:10
 * description:
 */
@Tag(name = "智能助手")
@RestController
@RequestMapping("/agent")
public class AgentController extends BaseController {

    @Resource
    private ChatAgent chatAgent;

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private RefinedResponseHolder refinedHolder;

    @Resource
    private ChatMessageFeignClient chatMessageFeignClient;

    @Operation(summary = "对话")
    @RateLimit
    @GlobalInterceptor
    @PostMapping(value = "/chat", produces = "text/event-stream;charset=utf-8")
    public Flux<String> chat(@RequestBody ChatForm chatForm) {
        if (StringUtils.isEmpty(chatForm.question())) {
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
        String memoryId = chatForm.memoryId();
        CompletableFuture<String> refinedFuture = refinedHolder.register(memoryId);
        return Flux.concat(
                chatAgent.chat(memoryId, chatForm.question()),
                Mono.fromFuture(refinedFuture)
                        .map(refined -> "\n__FINAL__" + refined)
                        .defaultIfEmpty("")
        );
    }

    @Operation(summary = "获取某个用户的对话记录")
    @RateLimit
    @GlobalInterceptor
    @GetMapping("/history")
    public ResponseVO history(@RequestParam("memoryId") String memoryId) {
        return getSuccessResponseVO(chatMessageService.getHistory(memoryId));
    }

    @Operation(summary = "生成会话标题")
    @GlobalInterceptor
    @GetMapping("/getTitle")
    public CompletableFuture<ResponseVO> getTitle(@RequestParam("memoryId") String memoryId) {
        return chatMessageService.getTitle(memoryId)
                .thenApply(title -> {
                    /*
                     * 更新会话内容
                     */
                    if (StringUtils.isEmpty(title)) {
                        throw new BusinessException(ResponseCodeEnum.CODE_500);
                    }
                    ChatSession chatSession = new ChatSession();
                    chatSession.setLastMessage(title);
                    String sessionId = StringUtils.getChatSessionId4User(new String[]{memoryId, Constants.ROBOT_UID});
                    this.chatMessageFeignClient.updateBySessionId(chatSession, sessionId);
                    return getSuccessResponseVO(title);
                });
    }

    @RateLimit
    @GetMapping("/test")
    public ResponseVO test() {
        return getSuccessResponseVO("测试成功");
    }
}