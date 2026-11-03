package com.example.serverprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 服务提供方启动类
 * 对外暴露 REST 接口，供 server-consumer 通过 OpenFeign 调用
 *
 * @EnableDiscoveryClient 启用服务注册发现，自动注册到 Nacos
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServerProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerProviderApplication.class, args);
    }

}
