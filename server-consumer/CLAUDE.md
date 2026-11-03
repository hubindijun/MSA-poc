# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

`server-consumer` is a Spring Boot 3.3.2 microservice (Java 17) acting as a "consumer" service within a CMS ecosystem. It calls the `server-provider` service via OpenFeign (discovered through Nacos) and exposes its own REST endpoints.

- Base package: `com.example.serversconsumer`
- Default port: 8081
- App entry point: `ServerConsumerApplication.java`
- Registered in Nacos as: `server-consumer`

## Architecture

```
com.example.serversconsumer
├── ServerConsumerApplication      # Spring Boot main class (@EnableFeignClients, @EnableDiscoveryClient)
├── client
│   └── ProviderClient             # Feign client → server-provider (discovered via Nacos, no hard-coded URL)
└── controller
    └── ConsumerController         # REST controller → /consumer/*
```

- **Controller layer** handles HTTP requests and delegates to Feign clients.
- **Feign client layer** (`ProviderClient`) calls `server-provider` using `@FeignClient(name = "server-provider")` — the service URL is resolved dynamically via Nacos + Spring Cloud LoadBalancer, no hard-coded URL.
- **Spring Cloud LoadBalancer** is required for Feign + service discovery to work (included as a direct dependency).
- Service-to-service calls return `Result<Object>` to accommodate generic JSON deserialization across process boundaries.
- **`cms-common`** (`com.cms:cms-common:1.0.0`) is a local/custom dependency providing shared utilities like `com.cms.common.base.Result`. It is not a public library.
- **Spring Cloud 2023.0.3 (Leyton)** + **Spring Cloud Alibaba 2023.0.1.2** provide OpenFeign, LoadBalancer, and Nacos integration.
- **Nacos Discovery** — the service registers itself and discovers `server-provider` through Nacos.
- **Nacos Config** — remote config is loaded via `spring.config.import: nacos:server-consumer.yaml` (Nacos dataId: `server-consumer.yaml`, group: `DEFAULT_GROUP`).
- **SpringDoc OpenAPI UI** (`springdoc-openapi-starter-webmvc-ui:2.3.0`) provides Swagger UI at runtime.

## Configuration

Key properties in `application.yml`:

| Property | Value | Purpose |
|----------|-------|---------|
| `spring.application.name` | `server-consumer` | Service name (also Nacos registration name) |
| `server.port` | `8081` | Server port |
| `spring.cloud.nacos.discovery.server-addr` | `127.0.0.1:8848` | Nacos discovery server address |
| `spring.cloud.nacos.config.server-addr` | `127.0.0.1:8848` | Nacos config server address |
| `spring.cloud.nacos.config.file-extension` | `yaml` | Remote config file format |
| `spring.config.import` | `nacos:server-consumer.yaml` | Import remote config from Nacos |

Note: There is no `provider.url` property — the provider address is resolved through Nacos service discovery.

## Build & Run

The project uses Maven with the Maven Wrapper (`mvnw` / `mvnw.cmd`).

**Prerequisites:**
1. Nacos Server must be running at `127.0.0.1:8848` (a docker-compose file is provided at `../nacos/docker-compose.yml`).
2. `server-provider` should be running and registered in Nacos before testing Feign calls.

```bash
# Start Nacos (optional, if not already running)
cd ../nacos && docker compose up -d

# Compile
./mvnw compile

# Run tests
./mvnw test

# Package as executable JAR
./mvnw package

# Run the application
./mvnw spring-boot:run

# Run a single test class
./mvnw test -Dtest=ServerProviderApplicationTests

# Clean build
./mvnw clean compile
```

> Start Nacos first, then `server-provider` (port 8080), then `server-consumer` (port 8081).

## Useful Endpoints (at runtime)

| Path | Purpose |
|------|---------|
| `GET /consumer/query` | Calls `server-provider`'s `/provider/query` via Feign (Nacos-discovered) and returns the result |
| `/swagger-ui.html` | Swagger UI (SpringDoc) |
| `/v3/api-docs` | OpenAPI JSON spec |

## Nacos

- Console: `http://localhost:8848/nacos` (default: `nacos/nacos`)
- Docker compose: `../nacos/docker-compose.yml` (Nacos v3.2.1 standalone, auth disabled)
- Data IDs managed in Nacos: `server-provider.yaml`, `server-consumer.yaml` (group: `DEFAULT_GROUP`)

## Conventions

- API responses use `Result<T>` from `com.cms.common.base.Result` as a uniform wrapper.
- Controllers use `@RestController` (not `@Controller`) — all endpoints return JSON.
- Dependencies are injected with `@Autowired` field injection.
- Feign clients use `@FeignClient(name = "...")` with Nacos-based service discovery (no URL property).
- Configuration is in `application.yml` (not `.properties`).
- Remote configuration is managed via Nacos Config using `spring.config.import` (not `bootstrap.yml`).
- Lombok is available as an optional dependency.
