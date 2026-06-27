package com.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * function:
 * author:聂佳鑫
 * date:2025/10/28 20:22
 */
@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.chat"})
@MapperScan({"com.chat.mapper"})
@EnableTransactionManagement
@EnableScheduling
public class ChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class,args);
    }
}