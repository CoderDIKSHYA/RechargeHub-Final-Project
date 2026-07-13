first drop the table users if already exists in the oracle database
drop table users;

then create sequence in oracle
CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1;


in the postman

post : http://localhost:8082/users/register

{
    "name": "Satakratu",
    "email": "satakratu@gmail.com",
    "password": "satakratu789",
    "phoneNumber": "7973297"
}

post : http://localhost:8082/users/login

{
    "email": "satakratu@gmail.com",
    "password": "satakratu789"
}

now go to postman, go to header, then write 
key= Authorization
value = Bearer <your_token_generated>
get: http://localhost:8082/users/1