# RechargeHub — Complete Testing Guide

## How to Start Everything

```bash
# From project root (where docker-compose.yml is)
docker-compose up --build
```

Wait ~3-5 minutes for all services to start. Then test each feature below.

---

## 1. ADMIN vs USER Role Separation

### Step 1 — Register a normal user
```
POST http://localhost:8989/users/register
Content-Type: application/json

{
  "name": "Test User",
  "email": "user@test.com",
  "password": "password123",
  "phoneNumber": "9876543210"
}
```
Response: `{ "id": 1, "role": "ROLE_USER", ... }`

### Step 2 — Login as user, get token
```
POST http://localhost:8989/users/login
Content-Type: application/json

{
  "email": "user@test.com",
  "password": "password123"
}
```
Response: `{ "token": "eyJ...", "user": {...} }`  
**Copy the token.**

### Step 3 — Try to create operator as USER (should FAIL with 403)
```
POST http://localhost:8989/operators
Authorization: Bearer <user-token>
Content-Type: application/json

{
  "name": "Jio",
  "type": "Prepaid",
  "circle": "India"
}
```
Expected: **403 Forbidden** ✅

### Step 4 — Register an ADMIN (first admin must be created directly in DB or via a bootstrap)
Since there's no admin yet, insert one directly:
```sql
-- Run in Oracle SQL Developer or via docker exec
INSERT INTO users2 (id, name, email, password, role, phone_number)
VALUES (user_seq.nextval, 'Admin', 'admin@rechargerhub.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHHi',
        'ROLE_ADMIN', '9999999999');
-- Password above is BCrypt of "admin123"
```

**OR** — use the admin register endpoint with an existing admin token:
```
POST http://localhost:8989/users/admin/register
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "name": "Admin User",
  "email": "admin@rechargerhub.com",
  "password": "admin123",
  "phoneNumber": "9999999999"
}
```

### Step 5 — Login as ADMIN
```
POST http://localhost:8989/users/login
{
  "email": "admin@rechargerhub.com",
  "password": "admin123"
}
```

### Step 6 — Create operator as ADMIN (should SUCCEED)
```
POST http://localhost:8989/operators
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "name": "Jio",
  "type": "Prepaid",
  "circle": "India"
}
```
Expected: **200 OK** with operator data ✅

### Step 7 — Get all users (ADMIN only)
```
GET http://localhost:8989/users/admin/all
Authorization: Bearer <admin-token>
```
Expected: List of all users ✅

---

## 2. Email + PDF Receipt — How to Test

### Full Flow (triggers email automatically):

**Step 1** — Register user with a REAL email address:
```
POST http://localhost:8989/users/register
{
  "name": "Dikshya",
  "email": "YOUR_REAL_EMAIL@gmail.com",
  "password": "password123",
  "phoneNumber": "9876543210"
}
```

**Step 2** — Login, get token

**Step 3** — Create operator + plan (as admin):
```
POST http://localhost:8989/operators
Authorization: Bearer <admin-token>
{ "name": "Jio", "type": "Prepaid", "circle": "India" }

POST http://localhost:8989/operators/1/plans
Authorization: Bearer <admin-token>
{ "amount": 299.0, "validity": "28 Days", "description": "Unlimited Calls + 2GB/day" }
```

**Step 4** — Initiate recharge (this triggers the full flow):
```
POST http://localhost:8989/recharges
Authorization: Bearer <user-token>
X-User-Id: 1
Content-Type: application/json

{
  "operatorId": 1,
  "planId": 1,
  "mobileNumber": "9876543210",
  "paymentMethod": "UPI"
}
```

**What happens automatically:**
1. Recharge created (PENDING)
2. Payment processed → Transaction saved → RabbitMQ event published
3. Notification-service receives event
4. PDF receipt generated (OpenPDF)
5. HTML email with PDF attachment sent to your email ✅

**Check your inbox** — you'll get an email like:
- Subject: `✅ RechargeHub: Recharge Successful — Receipt Attached`
- Body: HTML table with transaction details
- Attachment: `RechargeHub_Receipt_1.pdf`

---

## 3. Jacoco Code Coverage — How to Run

```bash
# Run from each service folder
cd user-service
mvn clean test

# Report generated at:
# user-service/target/site/jacoco/index.html
# Open in browser to see coverage %
```

### Automatic Coverage Generation (All Services)
Use the provided PowerShell script in the root directory:
```bash
./generate-coverage.ps1
```
This script will:
1. Iterate through all microservices.
2. Run `mvn clean test`.
3. Inform you if the service passed the coverage threshold (currently set to 100%).
4. Provide links to the HTML reports.

### How to see implemented tests?
- In each service, look at `src/test/java`.
- You can also check the JaCoCo report (`index.html`) which lists every class and method, showing exactly which lines are "covered" by tests (green) and which are not (red).
- The total number of tests run will be shown in the console output when running `mvn test` or the coverage script.

### Coverage Thresholds
> [!IMPORTANT]
> The current configuration requires **100% code coverage** for the build to pass. If you see "FAILED" in the coverage script, it usually means some code paths are not yet covered by tests. You can adjust this in the `pom.xml` of each service under the `jacoco-maven-plugin` configuration by changing the `<minimum>1.00</minimum>` value.


---

## 4. Zipkin Distributed Tracing

**URL:** http://localhost:9411

1. Make any API call (e.g., POST /recharges)
2. Open Zipkin UI
3. Click "Run Query"
4. You'll see the full trace: Gateway → Recharge → Payment → (async) Notification
5. Click any trace to see timing of each service call

---

## 5. Prometheus Metrics

**URL:** http://localhost:9090

Try these queries:
```
# HTTP request count per service
http_server_requests_seconds_count

# JVM memory usage
jvm_memory_used_bytes

# Active DB connections
hikaricp_connections_active
```

---

## 6. Grafana Dashboard

**URL:** http://localhost:3000  
**Login:** admin / admin

1. Go to Connections → Data Sources → Add Prometheus
2. URL: `http://prometheus:9090`
3. Save & Test
4. Create Dashboard → Add Panel → use Prometheus queries above

---

## 7. Circuit Breaker — How to Test

**Stop the payment-service:**
```bash
docker-compose stop payment-service
```

**Try to initiate a recharge:**
```
POST http://localhost:8989/recharges
Authorization: Bearer <token>
...
```

Expected: Circuit breaker triggers, returns fallback response:
```json
{
  "status": "CANCELLED",
  "message": "Payment system is currently unavailable. Please try again later."
}
```

**Restart payment-service:**
```bash
docker-compose start payment-service
```

---

## 8. RabbitMQ — How to Monitor

**URL:** http://localhost:15672  
**Login:** guest / guest

1. Go to **Queues** tab
2. You'll see `payment_queue`
3. After a recharge, check **Message rates** — you'll see messages published and consumed
4. Click the queue → **Get messages** to inspect message content

---

## 9. Swagger UI — All APIs in One Place

**URL:** http://localhost:8989/swagger-ui.html

Dropdown at top-right lets you switch between:
- user-service
- operator-service
- recharge-service
- payment-service
- notification-service

You can test all APIs directly from Swagger UI.

---

## 10. Cloudinary — Profile Picture Upload

```
PUT http://localhost:8989/users/profile/picture
Authorization: Bearer <user-token>
X-User-Id: 1
Content-Type: multipart/form-data

picture: [select any image file]
```

Response:
```json
{ "profilePictureUrl": "https://res.cloudinary.com/dv2njgzci/image/upload/..." }
```

---

## 11. Eureka Dashboard — Service Registry

**URL:** http://localhost:8761

You'll see all registered services:
- USER-SERVICE
- OPERATOR-SERVICE
- PAYMENT-SERVICE
- NOTIFICATION-SERVICE
- RECHARGE-SERVICE
- GATEWAY-SERVICE

---

## 12. Notification History — Check Saved Notifications

```
GET http://localhost:8989/api/notifications/user/1
Authorization: Bearer <user-token>
```

Returns all notifications for userId=1 with status (SENT/FAILED).

---

## Quick Postman Collection Setup

Import these as a Postman environment:
```
BASE_URL = http://localhost:8989
TOKEN = (paste after login)
ADMIN_TOKEN = (paste after admin login)
USER_ID = (from register response)
```

Then use `{{BASE_URL}}`, `{{TOKEN}}` in your requests.
