# 监控栈搭建与使用指南

Prometheus + Grafana 监控栈的搭建、验证、日常使用和常见问题。

## 目录

- [快速启动](#快速启动)
- [架构说明](#架构说明)
- [组件清单](#组件清单)
- [验证步骤](#验证步骤)
- [Grafana 使用](#grafana-使用)
- [Prometheus 使用](#prometheus-使用)
- [应用侧接入说明](#应用侧接入说明)
- [新增监控目标](#新增监控目标)
- [常见问题](#常见问题)

---

## 快速启动

### 前置条件

- Docker & Docker Compose 可用
- 业务服务（server-provider / server-consumer）已启动或即将启动

### 一键启动

```bash
cd monitoring
docker compose up -d
```

### 访问入口

| 组件 | 地址 | 默认账号 |
|------|------|---------|
| Prometheus UI | http://localhost:9090 | 无 |
| Grafana | http://localhost:3000 | admin / admin |

### 停止

```bash
docker compose down          # 停止并删除容器，数据卷保留
docker compose down -v       # 连数据卷一起删（清空历史数据）
```

---

## 架构说明

```
server-provider (:8080) ──┐
                           │  /actuator/prometheus
                           ├── pull (15s) ──► Prometheus (:9090) ──► Grafana (:3000)
                           │
server-consumer (:8081) ───┘
```

- **拉模型**：Prometheus 主动从应用拉取指标，应用被动暴露 HTTP 端点
- **静态配置**：目标实例写在 `prometheus.yml` 的 `static_configs` 中，没有服务发现组件
- **数据持久化**：Prometheus 和 Grafana 的数据都存在 Docker named volume 中，容器重建不丢数据

### 为什么不用 Nacos 做服务发现

Prometheus 官方不内置 Nacos 服务发现适配器。对于服务数量少、实例固定的场景，静态配置是最简单可靠的做法。如果后续需要动态发现，升级路径是 `file_sd_configs` + Nacos 同步脚本。

---

## 组件清单

### Prometheus

- 镜像：`prom/prometheus:v2.54.1`
- 端口：`9090`
- 配置文件：`prometheus/prometheus.yml`（挂载到 `/etc/prometheus/prometheus.yml`）
- 数据卷：`prometheus_data`（挂载到 `/prometheus`）
- 启用了 `--web.enable-lifecycle`，支持热加载配置

修改配置后热加载（不重启）：

```bash
curl -X POST http://localhost:9090/-/reload
```

### Grafana

- 镜像：`grafana/grafana-oss:11.2.0`
- 端口：`3000`
- 初始账号：`admin` / `admin`
- 数据卷：`grafana_data`
- **Provisioning 自动配置**：
  - 数据源：`grafana/provisioning/datasources/datasource.yml` — 自动配置 Prometheus 为默认数据源
  - Dashboard：`grafana/provisioning/dashboards/dashboard.yml` — 自动导入 `grafana/dashboards/` 下的所有 JSON
- 预置 Dashboard 都放在 **MSA POC** 文件夹下

---

## 验证步骤

启动后按顺序做以下检查：

### 1. 检查容器状态

```bash
docker compose ps
```

两个服务都应该是 `Up` 状态。

### 2. 验证应用指标端点

业务服务启动后，确认 metrics 端点可访问：

```bash
curl -s http://localhost:8080/actuator/prometheus | head -20
curl -s http://localhost:8081/actuator/prometheus | head -20
```

应该能看到类似 `jvm_memory_used_bytes`、`http_server_requests_seconds_*` 等指标行。

### 3. 验证 Prometheus 抓取状态

打开 http://localhost:9090/targets ，两个 job 的 state 都应该是 **UP**。

或者用命令行：

```bash
curl -s http://localhost:9090/api/v1/targets | python3 -m json.tool | grep '"health"'
```

### 4. 验证 Grafana

打开 http://localhost:3000 ，用 admin/admin 登录：

1. 左侧菜单 → Dashboards → 找到 **MSA POC** 文件夹
2. 进入 **JVM Overview** 或 **HTTP Requests**，应该能看到图表数据
3. 如果没有数据，先检查右上角时间范围（默认 Last 1 hour），以及应用是否已经有请求流过

---

## Grafana 使用

### 预置 Dashboard

| Dashboard | 内容 |
|-----------|------|
| **JVM Overview** | 堆内存、GC 频率与停顿、线程数、类加载数、CPU 使用率 |
| **HTTP Requests** | QPS、P50/P95/P99 延迟、5xx 错误率、Top 接口列表 |

每个 Dashboard 顶部都有 `application` 下拉框，可以按服务筛选。

### 修改 Dashboard 后保存

直接在 Grafana UI 里修改的 Dashboard **不会自动写回 JSON 文件**。如果要持久化修改：

1. Dashboard 设置 → JSON Model → 复制 JSON
2. 替换 `grafana/dashboards/` 下对应的文件
3. 重启 Grafana 或在 Provisioning 里触发重新加载

或者直接在 UI 里 Save，数据存在 Grafana 数据卷里，不影响使用，只是不会随仓库版本化。

### 添加新 Dashboard

1. 把 JSON 文件放到 `grafana/dashboards/` 下
2. 重启 Grafana：`docker compose restart grafana`
3. 在 Dashboard 浏览页的 **MSA POC** 文件夹里就能看到

---

## Prometheus 使用

### 常用 PromQL 查询

**JVM 堆使用率：**
```promql
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}
```

**服务 QPS：**
```promql
rate(http_server_requests_seconds_count[5m])
```

**P95 延迟：**
```promql
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))
```

**错误率：**
```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))
```

**实例存活状态：**
```promql
up
```

### 在 UI 里调试

打开 http://localhost:9090/graph ，输入 PromQL 表达式，点击 Execute，切换到 Graph 标签看图。

---

## 应用侧接入说明

两个服务已经完成接入，以下是接入要点，供后续新增服务参考：

### 依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "health,info,prometheus,metrics"
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: ${spring.application.name}
```

关键：`metrics.tags.application` 给所有指标加上全局的 `application` 标签，Grafana Dashboard 的变量筛选依赖这个标签。

### 开箱即用的指标

Spring Boot Actuator + Micrometer 自动暴露的常用指标：

| 指标前缀 | 类别 |
|---------|------|
| `jvm_*` | JVM 内存、GC、线程、类加载 |
| `http_server_requests_seconds_*` | HTTP 请求（count / sum / bucket） |
| `process_*` | 进程级 CPU、文件描述符、启动时间 |
| `system_*` | 系统级 CPU、负载 |
| `logback_events_*` | 日志级别计数 |
| `tomcat_*` | Tomcat 线程池、会话 |
| `spring_security_*` | Spring Security（如果引入了） |

---

## 新增监控目标

新增一个 Spring Boot 服务到监控，两步：

### 步骤 1：应用侧接入

参考上一节，加依赖加配置。

### 步骤 2：Prometheus 加 target

编辑 `prometheus/prometheus.yml`，在 `scrape_configs` 下追加：

```yaml
  - job_name: 'new-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8082']
        labels:
          application: new-service
```

热加载配置：

```bash
curl -X POST http://localhost:9090/-/reload
```

> 注意：Mac / Windows 下 Docker 访问宿主机用 `host.docker.internal`。如果 Prometheus 和业务服务在同一个 Docker 网络里，用服务名或容器名代替。

---

## 常见问题

### Q: Prometheus 的 Target 显示 DOWN，连接超时

大概率是网络问题。Prometheus 在容器里，要访问宿主机上跑的应用，地址用 `host.docker.internal` 而不是 `localhost`。

验证：在 Prometheus 容器里 curl 一下宿主机：
```bash
docker exec prometheus curl -s http://host.docker.internal:8080/actuator/prometheus | head -5
```

如果这个不通，检查应用是否真的监听在 `0.0.0.0` 上（Spring Boot 默认就是，一般没问题），以及端口有没有冲突。

### Q: Grafana Dashboard 没有数据，显示 "No data"

按顺序排查：

1. 应用启动了吗？`curl localhost:8080/actuator/prometheus` 有内容吗？
2. Prometheus 的 Target 是 UP 的吗？http://localhost:9090/targets
3. 时间范围对吗？右上角选择 "Last 5 minutes" 试试
4. Dashboard 顶部的 `application` 变量选对了吗？

### Q: 修改了 prometheus.yml 怎么生效？

优先热加载，不丢数据：
```bash
curl -X POST http://localhost:9090/-/reload
```

热加载失败或者改了启动参数时，再重启容器：
```bash
docker compose restart prometheus
```

### Q: Grafana 登录密码忘了

重置（会清空 Grafana 数据卷里的所有自定义配置，谨慎用）：
```bash
docker compose down -v
docker compose up -d
```

或者进容器用 CLI 改密码：
```bash
docker exec -it grafana grafana-cli admin reset-admin-password newpassword
```

### Q: 数据存多久？会占满磁盘吗？

默认 Prometheus 保留 15 天数据（`--storage.tsdb.retention.time=15d`，默认值）。对于 PoC 项目完全够用。

如果要调整，在 `docker-compose.yml` 的 `command` 里加：
```yaml
command:
  - '--storage.tsdb.retention.time=7d'
```

### Q: 监控本身挂了怎么发现？

Prometheus 自己也是一个监控目标（默认配置中有 `prometheus` job 监控自己）。如果要看监控系统本身的健康度，可以在 Grafana 加一个 "Monitor the Monitor" 的面板，或者用外部的健康检查（如 UptimeRobot）盯一下 9090 和 3000 端口。

生产环境的话，通常至少部署两个 Prometheus 实例做高可用，或者用 VictoriaMetrics / Thanos。

### Q: 能加告警吗？

可以。Prometheus 支持告警规则（`rule_files`），配合 Alertmanager 发邮件/钉钉/企业微信。PoC 阶段没有配置，有需要再加。
