# Nacos 本地环境（Docker Compose）

## 版本信息

- Nacos: **v3.2.1**（单机模式，内嵌 Derby 存储）
- 架构: linux/amd64（Mac Apple Silicon 通过 Rosetta 转译运行）

## 快速启动

```bash
# 进入目录
cd nacos

# 启动
docker compose up -d

# 查看日志
docker compose logs -f nacos

# 停止
docker compose down
```

## 访问地址

| 用途 | 地址 | 说明 |
|------|------|------|
| 控制台 UI | http://localhost:8849/ | 3.x 控制台独立端口，宿主机映射 8849 → 容器 8080 |
| Open API | http://localhost:8848/nacos | 客户端 SDK 连接地址 |
| gRPC | 9848 / 9849 | 服务发现、配置推送通信端口 |

## 登录信息

- **用户名**: `nacos`
- **密码**: `nacos`

> 本地开发环境已关闭鉴权（`NACOS_AUTH_ENABLE=false`），上述账号仅用于登录控制台。

## 端口映射

| 宿主机端口 | 容器端口 | 协议 | 用途 |
|-----------|---------|------|------|
| 8848 | 8848 | HTTP | Nacos Server API |
| 8849 | 8080 | HTTP | Nacos Console 控制台 |
| 9848 | 9848 | gRPC | 客户端 gRPC 主端口 |
| 9849 | 9849 | gRPC | 客户端 gRPC 辅端口 |

## 数据持久化

数据和日志挂载到本地目录，容器重建不丢失：

- `./data` — 内嵌 Derby 数据库
- `./logs` — 运行日志

## 客户端接入（Spring Cloud）

```yaml
spring:
  cloud:
    nacos:
      server-addr: localhost:8848
      discovery:
        namespace: public
      config:
        server-addr: localhost:8848
        file-extension: yaml
```
