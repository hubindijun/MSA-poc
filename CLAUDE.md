# CLAUDE.md

This file provides guidance to Claude Code when working with the MSA-poc repository.

## Repository Overview

MSA-poc is a microservice architecture proof-of-concept with two Spring Boot services that communicate via Nacos service discovery and OpenFeign.

## Project Structure

```
MSA-poc/
├── nacos/                  # Nacos server (Docker Compose) - shared infrastructure
├── server-provider/        # Provider microservice (port 8080)
│   └── CLAUDE.md           # Service-specific details
└── server-consumer/        # Consumer microservice (port 8081)
    └── CLAUDE.md           # Service-specific details
```

Each service has its own `CLAUDE.md` with package structure, dependencies, endpoints, and conventions. Consult those for service-level details.

## Architecture

- **Nacos** serves as both service discovery server and configuration center.
- **server-provider** registers in Nacos and exposes REST endpoints under `/provider/*`.
- **server-consumer** discovers `server-provider` through Nacos and calls it via OpenFeign (`@FeignClient(name = "server-provider")`), with Spring Cloud LoadBalancer handling client-side load balancing.
- Both services pull remote config from Nacos via `spring.config.import: nacos:<service>.yaml`.
- `cms-common` (`com.cms:cms-common:1.0.0`) is a local/custom dependency providing shared utilities like `Result<T>`. It is not a public library.

## Startup Order

1. Nacos (`cd nacos && docker compose up -d`)
2. server-provider (port 8080)
3. server-consumer (port 8081)

## Tech Stack

- Spring Boot 3.3.2 / Java 17 / Maven
- Spring Cloud 2023.0.3 (Leyton)
- Spring Cloud Alibaba 2023.0.1.2
- Spring Cloud OpenFeign + Spring Cloud LoadBalancer
- Nacos v3.2.1 (standalone, Derby, auth disabled)
- SpringDoc OpenAPI 2.3.0
- Lombok
