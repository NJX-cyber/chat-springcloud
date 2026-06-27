package com.example.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 针对指定用户的熔断注解。
 * 从请求 token 中提取用户 ID，用 Resilience4j CircuitBreaker 按用户维度跟踪失败率并熔断。
 * 配置实例名映射到 application.yml 中 resilience4j.circuitbreaker.instances.{name}。
 *
 * <pre>
 * 用法：
 *   @CircuitBreak(name = "cb-comm")
 *   @PostMapping("/api/endpoint")
 *   public ResponseVO endpoint() { ... }
 * </pre>
 *
 * @author normal
 * @date 2026/6/17
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CircuitBreak {

    /**
     * Resilience4j CircuitBreaker 配置实例名
     */
    String name() default "cb-comm";
}
