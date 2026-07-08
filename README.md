<div align="center">

# Flash Sale Engine & API Rate Limiting Gateway

A production-inspired distributed backend system built to explore the engineering challenges behind high-concurrency flash sale platforms while following software engineering practices commonly used during real-world backend development.

[![Backend CI](https://github.com/shubhamskadam89/rate-limiter-redis/actions/workflows/ci.yml/badge.svg)](https://github.com/shubhamskadam89/rate-limiter-redis/actions/workflows/ci.yml)
[![Docker Backend](https://img.shields.io/docker/v/shubhamskadam89/flash-sale-backend?label=backend&logo=docker)](https://hub.docker.com/repository/docker/shubhamskadam89/flash-sale-backend/general)
[![Docker Frontend](https://img.shields.io/docker/v/shubhamskadam89/flash-sale-frontend?label=frontend&logo=docker)](https://hub.docker.com/repository/docker/shubhamskadam89/flash-sale-frontend/general)
[![Java 21](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)](https://redis.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

<br>

---

## Overview

Flash Sale Engine & API Rate Limiting Gateway is a production-inspired distributed backend system built as a hands-on exploration of how modern backend systems handle high concurrency, maintain consistency, and scale reliably.

The project was developed incrementally by studying real-world backend engineering patterns and understanding the problems they solve.

Each capability was then built by evaluating alternative approaches, implementing the chosen solution, validating it through testing and benchmarking, and documenting the engineering decisions behind it.

The platform consists of two complementary systems.

**API Rate Limiting Gateway**
A distributed, Redis-backed rate limiting gateway supporting multiple algorithms including Fixed Window, Sliding Window, and Token Bucket. The gateway applies endpoint-aware and role-aware policies, allowing different APIs and user roles to enforce different traffic limits while sharing state across multiple backend instances.

**Flash Sale Engine**
A high-concurrency purchase engine designed around consistency and correctness. It guarantees atomic inventory updates using Redis Lua scripts, prevents duplicate purchases through distributed idempotency, persists orders asynchronously to reduce request latency, broadcasts inventory changes in real time using Server-Sent Events (SSE), and supports horizontal scaling without sacrificing correctness.

Throughout the project, architectural decisions, trade-offs, benchmarks, implementation details, and lessons learned are documented to explain not only how each component works, but also why it exists and the engineering benefits it provides over simpler approaches.

<br>

---

## Why This Project Exists

Modern backend systems solve problems that rarely appear in traditional CRUD applications. Handling thousands of concurrent requests, preventing overselling, protecting APIs from abuse, processing retries safely, maintaining consistency across multiple instances, and observing system behaviour under load all require architectural decisions that go far beyond basic request-response programming.

This project began as an exploration of API rate limiting and gradually evolved into a complete flash sale platform as new engineering challenges emerged. Rather than implementing features in isolation, each major capability was introduced to solve a specific problem, understand the limitations of simpler approaches, evaluate alternative solutions, and validate the chosen design through testing and benchmarking.

The goal of this repository is to document that engineering journey — not just the final implementation. Every major decision is accompanied by its motivation, trade-offs, implementation details, validation strategy, and lessons learned, making the repository both a working system and a reference for understanding distributed backend engineering.

<br>

---

## Key Features

<table>
<tr>
<td valign="top" width="50%">

**Authentication & Authorization**
- JWT-based authentication with role-based access control (RBAC)
- Access and refresh token workflow
- Protected administrative endpoints

**API Rate Limiting Gateway**
- Runtime algorithm selection using the Strategy Pattern
- Endpoint-aware and role-aware rate limiting policies
- Fixed Window algorithm
- Sliding Window algorithm
- Token Bucket algorithm with Redis Lua
- Redis-backed distributed state shared across multiple application instances
- Dynamic rate limit configuration without application code changes

</td>
<td valign="top" width="50%">

**Flash Sale Engine**
- Atomic inventory management using Redis Lua scripts
- Zero oversell guarantee under concurrent load
- Distributed idempotent purchase processing
- Per-user purchase limits
- Asynchronous order persistence through Redis queues
- Real-time inventory updates using Server-Sent Events (SSE)

**Scalability & Reliability**
- Horizontal scaling behind Nginx with shared Redis state
- Dockerized multi-service deployment
- Background workers for asynchronous processing
- Inventory consistency validation under concurrent load

**Observability & Quality**
- Prometheus metrics and Grafana dashboards
- Comprehensive unit and integration testing
- Concurrency validation using multithreaded test suites
- k6 performance and load testing
- GitHub Actions CI/CD pipeline

</td>
</tr>
</table>

<br>

---

## Architecture Snapshot

Requests flow through a layered pipeline — authentication, distributed rate limiting, and idempotency checks — before reaching the business layer. Redis holds all concurrency-sensitive state (rate limit counters, inventory locks, idempotency keys), while MySQL is the system of record. Because the application layer is stateless and backed by Redis, it's designed to scale horizontally behind Nginx without sacrificing consistency.

A purchase request executes an atomic Redis Lua script to validate inventory and enforce per-user limits, queues the order for asynchronous persistence, and immediately broadcasts the updated stock count to connected clients via SSE — so the write path never blocks on the database.

```mermaid
flowchart LR
    Client["Client / React Frontend"]
    Nginx["Nginx"]
    Backend["Spring Boot API"]
    Redis[("Redis")]
    MySQL[("MySQL")]
    Worker["Background Worker"]

    Client -->|HTTP| Nginx
    Nginx --> Backend
    Backend <--> Redis
    Backend --> Worker
    Worker --> MySQL
    Backend -.->|SSE stock updates| Client
```

```mermaid
flowchart TD
    A[Client Request] --> B[Authentication]
    B --> C[Rate Limiter]
    C --> D[Idempotency Check]
    D --> E[Flash Sale Service]
    E --> F["Redis Lua Script — atomic inventory check"]
    F --> G[Redis Queue]
    F --> H[SSE Broadcast]
    G --> I[Background Worker]
    I --> J[(MySQL)]
```

> 📖 Full request-lifecycle sequence diagrams, deployment topology, and architectural decisions live in [docs/architecture.md](docs/architecture.md).

<br>

---

## Demo

The following screenshots demonstrate the core workflows implemented in the project.

<table>
<tr>
<th align="center">Authentication</th>
<th align="center">Purchase Workspace</th>
</tr>
<tr>
<td><img src="https://github.com/user-attachments/assets/d0ae93df-bc6a-41c9-9c20-52a2a4bf3571" alt="Authentication" width="500" /></td>
<td><img src="https://github.com/user-attachments/assets/7313173e-1ce1-44a4-b68b-0c090c5b1db3" alt="Purchase Workspace" width="500" /></td>
</tr>
<tr>
<th align="center">Administration</th>
<th align="center">Rate Limiting</th>
</tr>
<tr>
<td><img src="https://github.com/user-attachments/assets/79843cc1-668e-411f-ab29-f4e7457fbfcc" alt="Sale Console" width="500" /></td>
<td><img src="https://github.com/user-attachments/assets/9ffb9fec-9b0b-46bc-99cd-4fc4e5d7ae2a" alt="Purchase" width="500" /></td>
</tr>
<tr>
<th align="center">Observability</th>
<th align="center">Continuous Integration</th>
</tr>
<tr>
<td><img src="https://github.com/user-attachments/assets/95c144b0-ed4a-4ae0-932a-10474d04edb7" alt="Grafana Dashboard" width="500" /></td>
<td><img src="https://github.com/user-attachments/assets/e1dacdba-a906-48d2-b473-6e2426b99ff2" alt="GitHub Actions Workflows" width="500" /></td>
</tr>
</table>

<br>

---

## Getting Started

### Option 1 — Run with Docker <sub>(Recommended)</sub>

> The fastest way to explore the application. Pre-built images are pulled from Docker Hub.
> No Java, Maven, or Node.js installation required.
>
> **Best for:** Recruiters, interviewers, and anyone evaluating the project.

**Prerequisites:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose)

```bash
# 1. Clone the repository (needed for config files — nginx, prometheus, grafana)
git clone https://github.com/shubhamskadam89/rate-limiter-redis.git
cd rate-limiter-redis

# 2. Copy the environment file
cp .env.example .env

# 3. Start the full stack
docker compose up -d
```

That's it. Docker pulls the backend and frontend images from Docker Hub automatically.

| Service | URL |
|---|---|
| Application | `http://localhost` |
| Swagger UI | `http://localhost/swagger-ui/index.html` |
| Grafana | `http://localhost/grafana` &nbsp;*(admin / admin)* |
| Prometheus | `http://localhost:9090` |

> **Default credentials** — Admin: `admin@example.com` / `password` &nbsp;·&nbsp; User: `user@example.com` / `password`

<br>

### Option 2 — Build from Source

> Run the project locally for development or to explore and modify the implementation.
>
> **Best for:** Developers, contributors, and anyone learning the codebase.

**Prerequisites:** Java 21, Maven, Node.js 22, Docker Desktop

```bash
# 1. Clone the repository
git clone https://github.com/shubhamskadam89/rate-limiter-redis.git
cd rate-limiter-redis

# 2. Copy the environment file
cp .env.example .env

# 3. Start infrastructure (MySQL, Redis, Nginx, Prometheus, Grafana)
cd docker && docker compose up -d mysql redis prometheus grafana nginx

# 4. Run the backend
cd ../backend
./mvnw spring-boot:run

# 5. Run the frontend (in a separate terminal)
cd ../frontend
npm install
npm run dev
```

The frontend dev server starts at `http://localhost:5173`.

<br>

---

## Tech Stack

| Layer | Technologies |
|--------|--------------|
| Backend | Spring Boot 3, Java 21 |
| Frontend | React, TypeScript, Vite |
| Security | Spring Security, JWT |
| Database | MySQL 8 |
| Distributed State | Redis 7 |
| Rate Limiting | Fixed Window, Sliding Window, Token Bucket (Lua) |
| Messaging | Redis Lists |
| Real-Time Communication | Server-Sent Events (SSE) |
| Observability | Micrometer, Prometheus, Grafana |
| Testing | JUnit 5, Mockito, Testcontainers, k6 |
| DevOps | Docker, Docker Compose, Nginx, GitHub Actions |

<br>

---

## Engineering Decisions at a Glance

- Runtime algorithm selection using the Strategy Pattern for endpoint- and role-aware rate limiting
- Three interchangeable Redis-backed rate limiting algorithms with configuration-driven policies
- Atomic purchase execution using Redis Lua scripts to guarantee inventory consistency
- Distributed idempotency ensuring safe client retries without duplicate orders
- Asynchronous order persistence to keep the purchase path lightweight
- Real-time inventory synchronization using Server-Sent Events (SSE)
- Horizontal scaling using stateless application instances with shared Redis state
- Observability through Micrometer, Prometheus, and Grafana
- Automated testing covering unit, integration, concurrency, and load-testing scenarios
- Dockerized deployment with CI/CD pipelines publishing application images

<br>

---

## Documentation

This project is accompanied by engineering documentation that explains not only how the system works, but also why major architectural decisions were made, the trade-offs involved, and how those decisions were validated.

| Document | Description |
|----------|-------------|
| Architecture | Overall system design and request lifecycle |
| API Guide | Authentication, purchase flow, SSE and endpoints |
| Engineering Decisions | Rationale behind major architectural choices |
| Performance | Benchmark methodology and results |
| Deployment | Local setup and production-like deployment |
| Troubleshooting | Common failures and diagnostics |
| Local Development | Development workflow and common commands |

<br>

---

## Roadmap

**Current Capabilities**

- [x] JWT Authentication & RBAC
- [x] Runtime-configurable Rate Limiting
- [x] Flash Sale Engine
- [x] Redis Lua Atomic Purchase Flow
- [x] Distributed Idempotency
- [x] Asynchronous Order Processing
- [x] Real-Time Inventory Streaming
- [x] Observability
- [x] Horizontal Scaling
- [x] CI/CD

**Future Roadmap** <sub>(Post v1.0)</sub>

The primary focus of **v1.0** is to deliver a fully implemented, validated, and documented modular monolith. Future work will build upon this foundation by exploring larger-scale distributed architectures and operational capabilities.

- [ ] Redis Sentinel for high availability
- [ ] MySQL Read Replicas for read scaling
- [ ] Kubernetes deployment and orchestration
- [ ] Redis Cluster for distributed data partitioning
- [ ] Horizontal autoscaling
- [ ] Distributed performance benchmarking
- [ ] OpenTelemetry-based distributed tracing
- [ ] Chaos engineering and resilience testing

> ### Engineering Principle
>
> Every engineering claim in this repository is backed by implementation, configuration, benchmark results, tests, logs, metrics, or documentation. Features that have not been implemented or validated are intentionally deferred to future milestones rather than being presented as part of the current system.

<br>

---

## Contributing

Feedback, bug reports, feature suggestions, and engineering discussions are always welcome. If you discover an issue, have an idea for improvement, or would like to discuss an architectural decision, feel free to open an issue or start a discussion.

For larger changes, please open an issue first so the proposed approach can be discussed before implementation.

<br>

---

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

<br>

---

<div align="center">

*Learning distributed systems by building them, validating them, and documenting the engineering decisions behind them.*

</div>