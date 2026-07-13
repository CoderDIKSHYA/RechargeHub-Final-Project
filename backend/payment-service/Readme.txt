=========================================================
PAYMENT SERVICE - API ENDPOINTS & RUN INSTRUCTIONS
=========================================================

---------------------------------------------------------
1. PREREQUISITES TO RUN THE SERVICE
---------------------------------------------------------
Before running the Payment Service, ensure the following are up and running:
1. Oracle Database (XE or 11g) - Ensure credentials in application.yml match.
2. RabbitMQ Server - Required for async messaging to Notification Service.
3. Eureka Discovery Server - Required for service registration.

Also, run this sequence in your Oracle Database before starting the application:
CREATE SEQUENCE transaction_seq
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

To run the application:
1. Open terminal in the `payment-service` directory.
2. Run: `mvn clean spring-boot:run`
(Default Port: 8084)


---------------------------------------------------------
2. API ENDPOINTS EXAMPLES (JSON)
---------------------------------------------------------
Base URL: http://localhost:8084/api/payments

1. PROCESS A NEW PAYMENT (POST request)
URL: http://localhost:8084/api/payments
Headers: Content-Type: application/json
Body (JSON):
{
  "rechargeId": 101,
  "userId": 501,
  "amount": 299.00,
  "paymentMethod": "UPI"
}

2. GET PAYMENT BY ID (GET request)
URL: http://localhost:8084/api/payments/1
Response (JSON):
{
  "id": 1,
  "rechargeId": 101,
  "userId": 501,
  "amount": 299.00,
  "paymentMethod": "UPI",
  "status": "SUCCESS",
  "transactionTime": "2026-03-21T10:15:30"
}

3. GET PAYMENTS BY USER ID (GET request)
URL: http://localhost:8084/api/payments/user/501
Response (JSON):
[
  {
    "id": 1,
    "rechargeId": 101,
    "userId": 501,
    "amount": 299.00,
    "paymentMethod": "UPI",
    "status": "SUCCESS",
    "transactionTime": "2026-03-21T10:15:30"
  }
]

4. GET PAYMENTS BY RECHARGE ID (GET request)
URL: http://localhost:8084/api/payments/recharge/101
Response (JSON):
[
  {
    "id": 1,
    "rechargeId": 101,
    "userId": 501,
    "amount": 299.00,
    "paymentMethod": "UPI",
    "status": "SUCCESS",
    "transactionTime": "2026-03-21T10:15:30"
  }
]
