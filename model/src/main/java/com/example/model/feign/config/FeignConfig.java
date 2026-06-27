package com.example.model.feign.config;

import com.example.model.enums.ResponseCodeEnum;
import com.example.model.exception.BusinessException;
import com.example.model.utils.JsonUtils;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Feign 全局配置（含自定义异常解码器）
 * author:normal
 * date:2026/6/15
 */
@Configuration
public class FeignConfig {

    private static final Logger logger = LoggerFactory.getLogger(FeignConfig.class);

    /**
     * 自定义 Feign 异常解码器
     * 将远程服务返回的错误信息转换为 BusinessException
     */
    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return new ErrorDecoder() {
            @Override
            public Exception decode(String methodKey, Response response) {
                logger.error("Feign 调用异常, methodKey={}, status={}", methodKey, response.status());

                try {
                    if (response.body() != null) {
                        String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                        logger.error("Feign 错误响应体: {}", body);

                        // 尝试解析为 ResponseVO
                        try {
                            com.example.model.entity.vo.ResponseVO<?> responseVO =
                                    JsonUtils.convertJson2Obj(body, com.example.model.entity.vo.ResponseVO.class);
                            if (responseVO != null && responseVO.getCode() != null) {
                                return new BusinessException(responseVO.getCode(), responseVO.getInfo());
                            }
                        } catch (Exception parseEx) {
                            logger.warn("无法解析 Feign 错误响应体为 ResponseVO: {}", parseEx.getMessage());
                        }

                        return new BusinessException(ResponseCodeEnum.CODE_500.getCode(),
                                "远程服务调用失败: " + body);
                    }
                } catch (IOException e) {
                    logger.error("读取 Feign 错误响应体失败", e);
                }

                return new BusinessException(ResponseCodeEnum.CODE_500.getCode(),
                        "远程服务调用失败, HTTP状态码: " + response.status());
            }
        };
    }
}
