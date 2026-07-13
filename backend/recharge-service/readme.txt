# RechargeNova: Recharge Service README

## Overview
The `recharge-service` is a microservice responsible for handling mobile recharge requests within the RechargeNova platform. It communicates with other microservices and persists recharge data to an Oracle database.

## Prerequisites
1. Oracle Database running locally at `localhost:1521:XE`
   - Username: `scott`
   - Password: `DEVI`
2. Eureka Server (`service-registry`) running at `http://localhost:8761/eureka/`
3. JDK 17+ and Maven 3.6+ installed.

## Steps to Start the Service
1. **Start Eureka Server**: Ensure the `eureka-server` project is running so this service can register itself.
2. **Database Config**: Make sure Oracle DB is up and the `scott` user is accessible. The application is set to automatically update the schema (`spring.jpa.hibernate.ddl-auto=update`).
3. **Build the Project**: Navigate to the `recharge-service` directory and run:
   ```bash
   mvn clean install
   ```
4. **Run the Application**: You can start the application via your IDE (Run `RechargeServiceApplication.java`), or using Maven:
   ```bash
   mvn spring-boot:run
   ```
5. The service will start on port **8086**.

---

CREATE SEQUENCE recharge_seq
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

## API Test Endpoints

The `recharge-service` runs on `http://localhost:8086`. 
*Note: In a full microservice architecture, these are typically accessed via the API Gateway.*

### 1. Initiate a Recharge
- **URL**: `http://localhost:8086/recharges`
- **Method**: `POST`
- **Description**: Initiates a new mobile recharge request for a user.
- **Headers**:
  - `Content-Type: application/json`
  - `X-User-Id: <user_id>` (Required: This header is usually injected by the API Gateway after JWT validation. Provide a mock User ID, e.g., `1`, for direct testing.)
- **Request Body**:
  ```json
  {
      "operatorId": 1,
      "planId": 1,
      "mobileNumber": "9876543210",
      "paymentMethod": "UPI"
  }
  ```
- **Response**: `200 OK`
  ```json
  {
      "id": 1,
      "userId": 1,
      "operatorId": 1,
      "planId": 1,
      "mobileNumber": "9876543210",
      "amount": 299.00,
      "status": "INITIATED",
      "createdAt": "2026-03-21T10:00:00",
      "message": "Recharge initiated successfully"
  }
  ```

### 2. Get Recharge by ID
- **URL**: `http://localhost:8086/recharges/{id}`
- **Method**: `GET`
- **Description**: Retrieves the details of a specific recharge transaction.
- **Example URL**: `http://localhost:8086/recharges/1`
- **Response**: `200 OK` (similar object as above)

### 3. Get All Recharges for a User
- **URL**: `/recharges/user/{userId}`
- **Method**: `GET`
- **Description**: Retrieves a list of all recharge transactions associated with a specific user.
- **Example URL**: `http://localhost:8086/recharges/user/1`
- **Response**: `200 OK`
  ```json
  [
      {
          "id": 1,
          "userId": 1,
          "operatorId": 1,
          "planId": 1,
          "mobileNumber": "9876543210",
          "amount": 299.00,
          "status": "SUCCESS",
          "createdAt": "2026-03-21T10:00:00",
          "message": "Recharge successful"
      }
  ]
  ```

## Service Communication
- **Eureka Discovery**: Registers with Eureka registry on startup.
- **Feign Client**: Uses Feign to synchronously communicate with other microservices (e.g., `user-service`, `operator-service`, `payment-service` if applicable).
- **Timeouts**: Feign client read and connect timeouts are configured to 5000ms.
