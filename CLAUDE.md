# CLAUDE.md

This file provides guidance to Claude Code when working with the MSA-poc repository.

## Repository Overview

MSA-poc is a microservice architecture proof-of-concept with two Spring Boot services that communicate via Nacos service discovery and OpenFeign.

## Project Structure

```
MSA-poc/
├── nacos/                  # Nacos server (Docker Compose) - shared infrastructure
├── monitoring/             # Prometheus + Grafana (Docker Compose) - observability stack
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
- Both services define their own `Result<T>` uniform response wrapper locally under `common.Result`.
- **Observability:** Both services expose metrics via Spring Boot Actuator + Micrometer Prometheus at `/actuator/prometheus`. Prometheus scrapes both services (static targets), and Grafana visualizes with pre-provisioned dashboards.

## Monitoring Stack

| Component | Port / Path | Purpose |
|-----------|-------------|---------|
| Prometheus UI | `http://localhost:9090` | Metrics storage & query |
| Grafana | `http://localhost:3000` (admin/admin) | Dashboards & visualization |
| server-provider metrics | `http://localhost:8080/actuator/prometheus` | Provider service metrics endpoint |
| server-consumer metrics | `http://localhost:8081/actuator/prometheus` | Consumer service metrics endpoint |

Prometheus uses static `static_configs` targets (no service discovery). Dashboard JSON files live in `monitoring/grafana/dashboards/` and are auto-imported via Grafana provisioning.

## Startup Order

1. Nacos (`cd nacos && docker compose up -d`)
2. Prometheus + Grafana (`cd monitoring && docker compose up -d`)
3. server-provider (port 8080)
4. server-consumer (port 8081)

## Tech Stack

- Spring Boot 3.3.2 / Java 17 / Maven
- Spring Cloud 2023.0.3 (Leyton)
- Spring Cloud Alibaba 2023.0.1.2
- Spring Cloud OpenFeign + Spring Cloud LoadBalancer
- Nacos v3.2.1 (standalone, Derby, auth disabled)
- SpringDoc OpenAPI 2.3.0
- Lombok
- Spring Boot Actuator + Micrometer Prometheus
- Prometheus v2.54.1
- Grafana 11.2.0 (OSS)
