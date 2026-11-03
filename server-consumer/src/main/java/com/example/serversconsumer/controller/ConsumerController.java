package com.example.serversconsumer.controller;

import com.cms.common.base.Result;
import com.example.serversconsumer.client.ProviderClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务消费方 Controller
 * 对外暴露 /consumer 前缀的 REST 接口，内部通过 Feign 调用 provider 服务
 */
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    /**
     * server-provider 的 Feign 客户端，由 Spring 自动注入
     */
    @Autowired
    private ProviderClient providerClient;

    /**
     * 查询接口 - 通过 Feign 调用 provider 服务的 /provider/query
     *
     * @return provider 返回的统一响应体，透传给前端
     */
    @GetMapping("/query")
    public Result<Object> query() {
        return providerClient.query();
    }
}
