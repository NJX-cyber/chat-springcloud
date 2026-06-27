package com.example.usermanager;

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
 * date:2026/6/15 15:31
 * description:
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.model.feign")
@ComponentScan(basePackages = {"com.example.usermanager", "com.example.model"})
@MapperScan("com.example.usermanager.mapper")
@Import(FeignConfig.class)
@SpringBootApplication
public class UserInfoManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserInfoManagerApplication.class, args);
        System.out.println("启动成功");
    }

}