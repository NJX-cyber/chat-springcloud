package com.example.agent;

import com.example.model.feign.config.FeignConfig;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * author:normal
 * date:2026/5/27 14:54
 * description:
 */
@EnableAsync
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.model.feign")
@ComponentScan(basePackages = {"com.example.agent", "com.example.model"})
@MapperScan("com.example.agent.mapper")
@Import(FeignConfig.class)
@SpringBootApplication
@Slf4j
public class AgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class,args);
        log.info("===智能助手服务启动成功===");
    }
}