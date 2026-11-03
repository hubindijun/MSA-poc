package com.example.serversconsumer.client;

import com.example.serversconsumer.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * server-provider 服务的 Feign 客户端
 * 通过 Nacos 服务发现动态获取服务实例，无需硬编码 URL
 *
 * name: 服务提供方在 Nacos 中注册的服务名，Feign 会通过 LoadBalancer 自动路由
 *
 * 注意：跨服务调用返回值使用 Result<Object> 而非 Result<String>，
 *       因为 JSON 反序列化时泛型类型会擦除，Object 更稳妥，拿到数据后再按需转换
 */
@FeignClient(name = "server-provider")
public interface ProviderClient {

    /**
     * 调用 provider 服务的 /provider/query 接口
     *
     * @return provider 返回的统一响应体
     */
    @GetMapping("/provider/query")
    Result<Object> query();
}
