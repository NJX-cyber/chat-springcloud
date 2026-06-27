package com.example.agent.memory;

import com.example.agent.component.LLMService;
import com.example.agent.utils.Utils;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 对话摘要 + AI 回复质量优化服务。
 *
 * @author normal
 * @date 2026/6/17
 */
@Component
public class SummarizationService {

    private static final Logger logger = LoggerFactory.getLogger(SummarizationService.class);

    @Resource
    private LLMService llmService;


    public String summarize(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) return "";

        String text = buildHistoryText(messages);
        if (text.isBlank()) return "";

        logger.debug("开始生成对话摘要, 消息数={}", messages.size());

        try {
            String result = llmService.chat(SUMMARY_PROMPT + "\n" + text).get();
            return result != null ? result.trim() : "";
        } catch (Exception e) {
            logger.warn("摘要生成异常: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 用 LLM 对 AI 回复做两维度评分（简洁度 + 准确度，各 1-10 分）。
     * 任一项低于 8 分则调用优化 prompt 重写，最多重试 2 次。
     */
    public String refineResponse(String userQuestion, String aiResponse) {
        if (userQuestion == null || userQuestion.isBlank()) return aiResponse;
        if (aiResponse == null || aiResponse.isBlank()) return aiResponse;

        String current = aiResponse;
        for (int i = 0; i < 2; i++) {
            int[] scores = evaluate(current, userQuestion);
            int conciseness = scores[0];
            int accuracy = scores[1];

            logger.debug("AI回复评分 (第{}次): 简洁度={}, 准确度={}", i + 1, conciseness, accuracy);

            if (conciseness >= 6 && accuracy >= 6) {
                return current;
            }

            try {
                String prompt = REFINE_PROMPT
                        .replace("{question}", userQuestion)
                        .replace("{response}", current);
                String result = llmService.chat(prompt).get();
                if (result != null && !result.isBlank()) {
                    logger.info("AI回复已优化 (第{}次), 原长度={}, 新长度={}",
                            i + 1, current.length(), result.length());
                    current = result.trim();
                }
            } catch (Exception e) {
                logger.warn("AI回复优化异常 (第{}次): {}", i + 1, e.getMessage());
                break;
            }
        }
        return current;
    }

    /**
     * 调用 LLM 打分，返回 [简洁度, 准确度]
     */
    private int[] evaluate(String response, String question) {
        try {
            String prompt = EVAL_PROMPT
                    .replace("{question}", question)
                    .replace("{response}", response);
            String raw = llmService.chat(prompt).get();
            if (raw == null) return new int[]{5, 5};

            int conciseness = 5, accuracy = 5;
            for (String line : raw.split("\n")) {
                line = line.trim();
                if (line.startsWith("简洁度:")) {
                    conciseness = parseScore(line);
                } else if (line.startsWith("准确度:")) {
                    accuracy = parseScore(line);
                }
            }
            return new int[]{conciseness, accuracy};
        } catch (Exception e) {
            logger.warn("AI回复评分异常: {}", e.getMessage());
            return new int[]{5, 5};
        }
    }

    private int parseScore(String line) {
        try {
            // "简洁度: 8" → 8
            String num = line.replaceAll("[^0-9]", "");
            return num.isEmpty() ? 5 : Math.min(10, Math.max(1, Integer.parseInt(num)));
        } catch (Exception e) {
            return 5;
        }
    }


    private String buildHistoryText(List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage msg : messages) {
            if (msg instanceof UserMessage) {
                sb.append("用户: ").append(Utils.getContent(msg)).append("\n");
            } else if (msg instanceof AiMessage aiMsg) {
                if (aiMsg.hasToolExecutionRequests()) continue;
                sb.append("助手: ").append(aiMsg.text()).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private static final String SUMMARY_PROMPT = """
            请用一段简洁的中文总结以下对话历史的核心内容和关键信息。
            要求：
            1. 不超过150字
            2. 只包含实质性内容，不含客套话
            3. 保留关键的决策、结论和待办事项

            对话历史：
            """;

    private static final String EVAL_PROMPT = """
            请严格评估以下AI回复的质量，从两个维度各打 1-10 分（只输出两行，不要其他内容）：

            评分标准：
            - 简洁度：回复是否冗余啰嗦（10=极其精炼，1=充满废话和重复）
            - 准确度：是否精确回答了用户核心问题（10=完美命中，1=完全偏离）

            用户问题：{question}
            AI回复：{response}

            格式：
            简洁度: X
            准确度: X
            """;

    private static final String REFINE_PROMPT = """
            请优化以下AI回复，使其更简洁、更准确地回答用户问题：
            - 删除冗余表述和重复内容
            - 删除无关客套话
            - 确保直接针对用户的问题回答

            用户问题：{question}
            原始回复：{response}

            直接输出优化后的完整回复，不要解释：
            """;
}
