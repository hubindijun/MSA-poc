# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

`server-provider` is a Spring Boot 3.3.2 microservice (Java 17) acting as a "provider" service within a CMS ecosystem. It exposes REST endpoints consumed by the `server-consumer` service via OpenFeign.

- Base package: `com.example.serverprovider`
- Default port: 8080
- App entry point: `ServerProviderApplication.java`
- Registered in Nacos as: `server-provider`

## Architecture

```
com.example.serverprovider
├── ServerProviderApplication      # Spring Boot main class (@EnableDiscoveryClient)
└── controller
    └── ProviderController         # REST controller → /provider/*
```

- **Controller layer** handles HTTP requests; returns `Result<T>` wrappers from the `cms-common` dependency.
- **`cms-common`** (`com.cms:cms-common:1.0.0`) is a local/custom dependency providing shared utilities like `com.cms.common.base.Result`. It is not a public library.
- **Spring Cloud 2023.0.3 (Leyton)** + **Spring Cloud Alibaba 2023.0.1.2** provide Nacos integration.
- **Nacos Discovery** — the service registers itself with Nacos on startup (`@EnableDiscoveryClient`).
- **Nacos Config** — remote config is loaded via `spring.config.import: nacos:server-provider.yaml` (Nacos dataId: `server-provider.yaml`, group: `DEFAULT_GROUP`).
- **SpringDoc OpenAPI UI** (`springdoc-openapi-starter-webmvc-ui:2.3.0`) provides Swagger UI at runtime.

## Configuration

Key properties in `application.yml`:

| Property | Value | Purpose |
|----------|-------|---------|
| `spring.application.name` | `server-provider` | Service name (also Nacos registration name) |
| `server.port` | `8080` | Server port |
| `spring.cloud.nacos.discovery.server-addr` | `127.0.0.1:8848` | Nacos discovery server address |
| `spring.cloud.nacos.config.server-addr` | `127.0.0.1:8848` | Nacos config server address |
| `spring.cloud.nacos.config.file-extension` | `yaml` | Remote config file format |
| `spring.config.import` | `nacos:server-provider.yaml` | Import remote config from Nacos |

## Build & Run

The project uses Maven with the Maven Wrapper (`mvnw` / `mvnw.cmd`).

**Prerequisites:** Nacos Server must be running at `127.0.0.1:8848` before starting the service (or update the config to point to your Nacos instance).

```bash
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

## Useful Endpoints (at runtime)

| Path | Purpose |
|------|---------|
| `GET /provider/query` | Returns `Result.ok("Provider OK!")` — consumed by server-consumer via Feign |
| `/swagger-ui.html` | Swagger UI (SpringDoc) |
| `/v3/api-docs` | OpenAPI JSON spec |

## Nacos

- Console: `http://localhost:8848/nacos` (default: `nacos/nacos`)
- The project includes a docker-compose file at `../server-consumer/nacos/docker-compose.yml` for local Nacos v3.2.1 standalone.

## Conventions

- API responses use `Result<T>` from `com.cms.common.base.Result` as a uniform wrapper.
- Controllers use `@RestController` (not `@Controller`) — all endpoints return JSON.
- Dependencies are injected with `@Autowired` field injection.
- Configuration is in `application.yml` (not `.properties`).
- Remote configuration is managed via Nacos Config using `spring.config.import` (not `bootstrap.yml`).
- Lombok is available as an optional dependency.
