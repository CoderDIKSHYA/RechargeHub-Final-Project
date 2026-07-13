CREATE DATABASE IF NOT EXISTS rechargerhub_users;
USE rechargerhub_users;
CREATE TABLE IF NOT EXISTS users2 (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255),
    phone_number VARCHAR(255),
    profile_picture_url VARCHAR(255),
    created_at DATETIME(6),
    is_verified BOOLEAN DEFAULT FALSE NOT NULL
);
INSERT INTO users2 (name, email, password, role, phone_number, created_at, is_verified)
VALUES ('Admin', 'admin@rechargerhub.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHHi', 'ROLE_ADMIN', '9999999999', NOW(), true);
