package com.example.messagemanager;

import com.example.model.feign.config.FeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * author:normal
 * date:2026/6/15 16:09
 * description:
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.model.feign")
@ComponentScan(basePackages = {"com.example.messagemanager", "com.example.model"})
@MapperScan("com.example.messagemanager.mapper")
@Import(FeignConfig.class)
@SpringBootApplication
public class ChatMessageManagerApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(
                    ChatMessageManagerApplication.class,
                    args
            );
        } catch (Throwable e) {

            System.out.println("启动异常");

            e.printStackTrace();

        }
    }
}