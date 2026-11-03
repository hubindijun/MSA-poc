package com.example.serversconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 服务消费方启动类
 * 通过 OpenFeign 调用 server-provider 提供的接口
 *
 * @EnableFeignClients   启用 Feign 客户端，自动扫描 @FeignClient 注解的接口
 * @EnableDiscoveryClient 启用服务注册发现，从 Nacos 获取服务实例列表
 */
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class ServerConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerConsumerApplication.class, args);
    }

}
