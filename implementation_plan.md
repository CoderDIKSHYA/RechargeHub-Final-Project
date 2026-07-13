# Implementation Plan: Phase 6 - System Resilience & Observability

This phase focuses on enhancing the reliability and monitoring capabilities of the RechargeHub ecosystem. We will implement circuit breakers to prevent cascading failures and integrate distributed tracing to monitor inter-service communication.

## Proposed Changes

### [Core: Infrastructure]
#### [MODIFY] [docker-compose.yml](file:///d:/Capgemini-Training/Capg-AdvJava-Training/All-Capg-Workspaces/Capgemini-Sprint-1/sprint-1/RechargeHub/backend/docker-compose.yml)
- Add **Zipkin** service for distributed tracing collection.
- Update microservices to include Zipkin endpoint configuration.

---

### [API Gateway]
#### [MODIFY] [pom.xml](file:///d:/Capgemini-Training/Capg-AdvJava-Training/All-Capg-Workspaces/Capgemini-Sprint-1/sprint-1/RechargeHub/backend/api-gateway/pom.xml)
- Add `spring-cloud-starter-circuitbreaker-reactor-resilience4j`.

#### [MODIFY] [application.yml](file:///d:/Capgemini-Training/Capg-AdvJava-Training/All-Capg-Workspaces/Capgemini-Sprint-1/sprint-1/RechargeHub/backend/api-gateway/src/main/resources/application.yml)
- Configure Circuit Breaker filters for routes (e.g., `recharge-service`).
- Define fallback mechanisms (e.g., redirecting to a static "Service Unavailable" response).

---

### [Recharge Service]
#### [MODIFY] [pom.xml](file:///d:/Capgemini-Training/Capg-AdvJava-Training/All-Capg-Workspaces/Capgemini-Sprint-1/sprint-1/RechargeHub/backend/recharge-service/pom.xml)
- Add `spring-boot-starter-actuator` for monitoring endpoints.
- Add `io.micrometer:micrometer-tracing-bridge-brave` and `io.zipkin.reporter2:zipkin-reporter-brave` for tracing.

#### [MODIFY] [RechargeService.java](file:///d:/Capgemini-Training/Capg-AdvJava-Training/All-Capg-Workspaces/Capgemini-Sprint-1/sprint-1/RechargeHub/backend/recharge-service/src/main/java/com/capg/RechargeHub/service/RechargeService.java)
- Apply `@CircuitBreaker` and `@Retry` to methods calling external services (like `payment-service` or `notification-service`).

---

### [Observability]
#### [NEW] [Grafana/Prometheus (Optional/Future)]
- Prepare configuration for metric collection (Actuator endpoints).

## Verification Plan

### Automated Tests
- Run `mvn clean install` to ensure dependency compatibility.
- Use `docker-compose up -d zipkin` and verify access to the Zipkin UI (port 9411).

### Manual Verification
- **Circuit Breaker Test**: Manually stop the `notification-service` and verify that the `recharge-service` handles the failure without crashing (e.g., by logging a warning and continuing).
- **Tracing Test**: Perform a recharge flow and verify that the complete request trace (Gateway -> Recharge -> Payment) is visible in Zipkin.
