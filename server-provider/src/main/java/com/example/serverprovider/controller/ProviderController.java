package com.example.serverprovider.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.cms.common.base.Result;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务提供方 Controller
 * 提供 /provider 前缀下的 REST 接口，被 consumer 服务通过 Feign 调用
 */
@RestController
@RequestMapping("/provider")
public class ProviderController {

    /**
     * 健康检查/示例接口
     * 返回成功包装的字符串，用于验证服务是否正常运行
     *
     * @return 统一响应体，包含 "Provider OK!"
     */
    @GetMapping("/query")
    public Result<String> query() {
        return Result.ok("Provider OK!");
    }
}
