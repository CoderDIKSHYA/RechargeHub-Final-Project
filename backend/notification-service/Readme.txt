=========================================================
NOTIFICATION SERVICE - API ENDPOINTS & RUN INSTRUCTIONS
=========================================================

---------------------------------------------------------
1. PREREQUISITES TO RUN THE SERVICE
---------------------------------------------------------
Before running the Notification Service, ensure the following are up and running:
1. Oracle Database (XE or 11g) - Ensure credentials in application.yml match.
2. RabbitMQ Server - Required for consuming async Payment Events.
3. Eureka Discovery Server - Required for service registration.
4. Payment Service (Optional) - To trigger the RabbitMQ events.

Also, run this sequence in your Oracle Database before starting the application:
CREATE SEQUENCE notification_seq
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

To run the application:
1. Open terminal in the `notification-service` directory.
2. Run: `mvn clean spring-boot:run`
(Default Port: 8085)


---------------------------------------------------------
2. API ENDPOINTS EXAMPLES (JSON)
---------------------------------------------------------
Base URL: http://localhost:8085/api/notifications

1. GET ALL NOTIFICATIONS (GET request)
URL: http://localhost:8085/api/notifications
Response (JSON):
[
  {
    "id": 1,
    "userId": 501,
    "message": "Your payment for recharge ID 101 has SUCCESS.",
    "type": "SMS",
    "status": "SENT",
    "createdAt": "2026-03-21T10:15:35"
  }
]

2. GET NOTIFICATION BY ID (GET request)
URL: http://localhost:8085/api/notifications/1
Response (JSON):
{
  "id": 1,
  "userId": 501,
  "message": "Your payment for recharge ID 101 has SUCCESS.",
  "type": "SMS",
  "status": "SENT",
  "createdAt": "2026-03-21T10:15:35"
}

3. GET NOTIFICATIONS BY USER ID (GET request)
URL: http://localhost:8085/api/notifications/user/501
Response (JSON):
[
  {
    "id": 1,
    "userId": 501,
    "message": "Your payment for recharge ID 101 has SUCCESS.",
    "type": "SMS",
    "status": "SENT",
    "createdAt": "2026-03-21T10:15:35"
  }
]
