in the oracle

run these sequences before starting the application

CREATE SEQUENCE operator_seq
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;


CREATE SEQUENCE plan_seq
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-------------------------------------------------
API ENDPOINT EXAMPLES (JSON)
-------------------------------------------------

1. ADD A NEW OPERATOR (POST request)
URL: http://localhost:8083/operators
Headers: Content-Type: application/json
Body (JSON):
{
  "name": "Jio",
  "type": "Prepaid",
  "circle": "Maharashtra"
}


2. GET ALL OPERATORS (GET request)
URL: http://localhost:8083/operators
Response (JSON):
[
  {
    "id": 1,
    "name": "Jio",
    "type": "Prepaid",
    "circle": "Maharashtra",
    "plans": []
  }
]


3. ADD A NEW PLAN TO AN OPERATOR (POST request)
URL: http://localhost:8083/operators/1/plans
Headers: Content-Type: application/json
Body (JSON):
{
  "amount": 299.0,
  "validity": "28 Days",
  "description": "1.5GB/Day, Unlimited Calls"
}


4. GET SPECIFIC OPERATOR DETAILS (GET request)
URL: http://localhost:8083/operators/1
Response (JSON):
{
  "id": 1,
  "name": "Jio",
  "type": "Prepaid",
  "circle": "Maharashtra",
  "plans": [
    {
      "id": 1,
      "operatorId": 1,
      "amount": 299.0,
      "validity": "28 Days",
      "description": "1.5GB/Day, Unlimited Calls"
    }
  ]
}