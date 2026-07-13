# RechargeHub — Microservices Project

## Project Structure

```
RechargeHub/
├── backend/                    ← All Spring Boot microservices
│   ├── user-service/           (Port 8082) — Auth, JWT, User management
│   ├── operator-service/       (Port 8083) — Operators & Plans CRUD
│   ├── payment-service/        (Port 8084) — Payment processing + RabbitMQ
│   ├── notification-service/   (Port 8085) — Email + PDF receipts via RabbitMQ
│   ├── recharge-service/       (Port 8086) — Recharge orchestration
│   ├── eureka-server/          (Port 8761) — Service discovery
│   ├── gatewayservice/         (Port 8989) — API Gateway + JWT filter
│   ├── mvc-client/             (Port 8090) — Spring MVC Thymeleaf client
│   ├── docker-compose.yml      ← Run entire backend
│   └── .env                    ← Environment variables
└── frontend/                   ← Angular 17 SPA
    ├── src/app/
    │   ├── core/               ← Guards, interceptors, services, models
    │   ├── features/           ← auth, dashboard, recharge, history, notifications, profile, admin
    │   └── shared/layout/      ← Sidebar navigation
    └── proxy.conf.json         ← Proxy to gateway port 8989
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.x, Java 17 |
| Database | MySQL 8.0 (migrated from Oracle) |
| Messaging | RabbitMQ |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Security | JWT (HS256), Spring Security |
| Frontend | Angular 17, Standalone Components, Signals |
| Email | Spring Mail (Gmail SMTP) |
| PDF | OpenPDF |
| Image Upload | Cloudinary |
| Tracing | Zipkin |
| Metrics | Prometheus + Grafana |
| Code Coverage | JaCoCo (100% target) |
| Static Analysis | SonarQube |
| Containerization | Docker + Docker Compose |

---

## What Is Implemented

### Backend
- ✅ JWT Authentication (user-service) — register, login, token generation
- ✅ Role-based access — ROLE_USER and ROLE_ADMIN
- ✅ Admin endpoints — register admin, get all users, delete user
- ✅ Operator CRUD (admin only) — create/update/delete operators and plans
- ✅ Recharge workflow — parallel async fetch (CompletableFuture) + payment call
- ✅ Payment processing — saves transaction, publishes RabbitMQ event
- ✅ Notification service — consumes RabbitMQ, sends HTML email + PDF receipt
- ✅ Circuit Breaker (Resilience4j) — payment service fallback
- ✅ Cloudinary — profile picture + operator logo upload
- ✅ Swagger UI — aggregated at gateway http://localhost:8989/swagger-ui.html
- ✅ Zipkin tracing — all services
- ✅ Prometheus metrics — all services
- ✅ JUnit + Mockito tests — all services
- ✅ JaCoCo 100% coverage configured
- ✅ SonarQube plugin configured
- ✅ Log4j2 — Console + File appender (logs/ folder)
- ✅ MySQL migration — all entities use IDENTITY strategy

### Frontend (Angular)
- ✅ Login page — JWT stored in localStorage
- ✅ Register page — validation (name: letters only, phone: 10 digits)
- ✅ Dashboard — stats, recent recharges, notifications
- ✅ Recharge wizard — 5 steps (Mobile → Operator → Plan → Checkout → Success)
- ✅ Plans page — browse all plans by operator
- ✅ History page — full recharge history
- ✅ Notifications page — all notifications
- ✅ Profile page — user details
- ✅ Admin panel — operators CRUD, plans CRUD, users management
- ✅ Auth guard — redirects to login if not authenticated
- ✅ Admin guard — redirects to dashboard if not admin
- ✅ Auth interceptor — auto-attaches Bearer token to all requests

---

## How to Run

### Prerequisites
- Docker Desktop installed and running
- Node.js v18+ installed
- Angular CLI: `npm install -g @angular/cli`

### Step 1 — Setup .env
The `.env` file is already in `backend/` folder with credentials.

### Step 2 — Start Backend
```bash
cd backend
docker-compose up --build
```
First time takes 5-10 minutes. Wait for all services to appear in Eureka.

### Step 3 — Start Frontend
```bash
cd frontend
ng serve
```

### Step 4 — Access
| URL | What |
|---|---|
| http://localhost:4200 | Angular Frontend |
| http://localhost:8989/swagger-ui.html | Swagger UI (all services) |
| http://localhost:8761 | Eureka Dashboard |
| http://localhost:15672 | RabbitMQ (guest/guest) |
| http://localhost:9411 | Zipkin Tracing |
| http://localhost:9090 | Prometheus |
| http://localhost:3000 | Grafana (admin/admin) |

---

## First Time Setup — Create Admin User

After backend starts, insert admin into MySQL:

```bash
docker exec -it mysql-db mysql -u root -proot
```

```sql
USE rechargerhub_users;
INSERT INTO users2 (name, email, password, role, phone_number, created_at)
VALUES ('Admin', 'admin@rechargerhub.com',
'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHHi',
'ROLE_ADMIN', '9999999999', NOW());
```
> Password: `admin123`

---

## MySQL Workbench Connection

| Field | Value |
|---|---|
| Hostname | 127.0.0.1 |
| Port | 3307 |
| Username | root |
| Password | root |

> MySQL must be running via docker-compose first.

---

## All API Endpoints (via Gateway: http://localhost:8989)

### User Service
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /users/register | None | Register user |
| POST | /users/login | None | Login, get JWT |
| GET | /users/{id} | JWT | Get user by ID |
| PUT | /users/profile/picture | JWT | Upload profile picture |
| POST | /users/admin/register | ADMIN | Create admin |
| GET | /users/admin/all | ADMIN | Get all users |
| DELETE | /users/admin/{id} | ADMIN | Delete user |

### Operator Service
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | /operators | JWT | Get all operators |
| GET | /operators/{id} | JWT | Get operator by ID |
| GET | /operators/plans/{id} | JWT | Get plan by ID |
| POST | /operators | ADMIN | Create operator |
| PUT | /operators/{id} | ADMIN | Update operator |
| DELETE | /operators/{id} | ADMIN | Delete operator |
| POST | /operators/{id}/plans | ADMIN | Create plan |
| PUT | /operators/plans/{id} | ADMIN | Update plan |
| DELETE | /operators/plans/{id} | ADMIN | Delete plan |

### Recharge Service
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /recharges | JWT | Initiate recharge |
| GET | /recharges/{id} | JWT | Get recharge by ID |
| GET | /recharges/user/{userId} | JWT | Get user recharges |

### Payment Service
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /api/payments | JWT | Process payment |
| GET | /api/payments/{id} | JWT | Get payment by ID |
| GET | /api/payments/user/{userId} | JWT | Get payments by user |
| GET | /api/payments/recharge/{id} | JWT | Get payments by recharge |

### Notification Service
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | /api/notifications | JWT | Get all notifications |
| GET | /api/notifications/{id} | JWT | Get by ID |
| GET | /api/notifications/user/{userId} | JWT | Get by user |

---

## Validations

### UserRegistrationRequest
- `name` — letters and spaces only, no numbers (2-50 chars)
- `email` — valid email format
- `password` — minimum 6 characters
- `phoneNumber` — exactly 10 digits, starts with 6-9

### RechargeRequest
- `mobileNumber` — exactly 10 digits, starts with 6-9

### OperatorDto
- `name`, `type`, `circle` — letters and spaces only

---

## Extra Features (Beyond Requirements)

| Feature | Where |
|---|---|
| Zipkin Distributed Tracing | All services |
| Prometheus Metrics | All services |
| Grafana Dashboard | docker-compose |
| Circuit Breaker (Resilience4j) | Gateway + recharge-service |
| RabbitMQ Async Messaging | payment → notification |
| PDF Receipt Generation | notification-service (OpenPDF) |
| HTML Email with Attachment | notification-service |
| Cloudinary Image Upload | user-service + operator-service |
| Azure Deployment Config | azure-compose.yml |
| Docker Hub Push Script | deploy-all.ps1 |
| Swagger UI Aggregation | Gateway |
| Parallel Async Fetch | recharge-service AsyncFetchService |
| Admin/User Role Separation | user-service + operator-service |
| Angular Frontend | frontend/ folder |

---

## Pending / Known Issues

- Docker Desktop must be running before `docker-compose up`
- First admin must be inserted manually into MySQL (see above)
- Frontend proxy configured for local dev (port 4200 → 8989)
