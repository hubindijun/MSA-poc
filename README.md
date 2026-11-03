# MSA-poc

Microservice Architecture proof-of-concept with Spring Boot, Spring Cloud Alibaba Nacos, and OpenFeign.

## Architecture

```
MSA-poc/
├── nacos/              # Nacos server (Docker Compose) - service discovery + config center
├── server-provider/    # Provider service (port 8080) - exposes REST endpoints
└── server-consumer/    # Consumer service (port 8081) - calls provider via Feign + Nacos discovery
```

- **server-provider** registers itself in Nacos and provides `/provider/*` endpoints.
- **server-consumer** discovers `server-provider` through Nacos and calls it via OpenFeign (`@FeignClient(name = "server-provider")`).
- Both services use Nacos Config for remote configuration (`spring.config.import`).

## Quick Start

### 1. Start Nacos

```bash
cd nacos
docker compose up -d
```

Console: http://localhost:8848/nacos (nacos/nacos)

### 2. Start server-provider

```bash
cd server-provider
./mvnw spring-boot:run
```

Runs on port 8080.

### 3. Start server-consumer

```bash
cd server-consumer
./mvnw spring-boot:run
```

Runs on port 8081.

## Endpoints

| Service | Path | Description |
|---------|------|-------------|
| server-provider | `/provider/query` | Provider sample endpoint |
| server-consumer | `/consumer/query` | Calls provider via Feign and returns result |
| both | `/swagger-ui.html` | Swagger UI |
| both | `/v3/api-docs` | OpenAPI JSON spec |

## Tech Stack

- Spring Boot 3.3.2 / Java 17
- Spring Cloud 2023.0.3 (Leyton)
- Spring Cloud Alibaba 2023.0.1.2 (Nacos Discovery + Nacos Config)
- Spring Cloud OpenFeign + Spring Cloud LoadBalancer
- SpringDoc OpenAPI 2.3.0
- Lombok
