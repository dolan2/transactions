# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew bootRun          # Run the application
./gradlew build            # Full build (assemble + test)
./gradlew test             # Run all tests
./gradlew test --tests org.finance.transactions.SomeTest  # Run a single test class
./gradlew check            # Run all checks
./gradlew clean            # Clean build artifacts
```

On Windows use `gradlew.bat` instead of `./gradlew`.

## Architecture

Spring Boot 4 microservice for transaction management. Package root: `org.finance.transactions`.

**Infrastructure (compose.yaml + Testcontainers):**
- **MongoDB** — primary data store via Spring Data MongoDB
- **RabbitMQ** — async messaging via Spring AMQP
- Local dev uses Docker Compose; tests spin up both via Testcontainers automatically (see `TestTransactionsApplication`)

**AI layer:**
- Spring AI 2.0.0-M5 with the Anthropic Claude integration — used for AI-powered transaction processing features

**API:**
- Spring Web MVC REST endpoints
- SpringDoc OpenAPI 3.0.2 exposes Swagger UI

**Key stack versions:**
- Java 26, Spring Boot 4.0.6, Spring AI 2.0.0-M5
- Lombok is used throughout for boilerplate reduction

## Testing

Integration tests run against real MongoDB and RabbitMQ instances via Testcontainers — no mocking of infrastructure. `TestTransactionsApplication` bootstraps the full test environment with service connections wired automatically.
