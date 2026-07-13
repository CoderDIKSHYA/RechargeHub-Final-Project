-- =============================================================================
-- RECHARGEHUB PRODUCTION-READY SEED SCRIPT (Midnight Symphony Edition)
-- This script populates the database with rich, realistic dummy data.
-- =============================================================================

USE rechargehub_operators;

-- Clear existing data (optional, but ensures clean slate)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE plans;
TRUNCATE TABLE operators;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Insert Operators
INSERT INTO operators (id, name, type, circle, logo_url) VALUES 
(1, 'Reliance Jio', 'Prepaid', 'Karnataka', 'https://upload.wikimedia.org/wikipedia/commons/5/50/Reliance_Jio_Logo_%28October_2015%29.svg'),
(2, 'Airtel', 'Prepaid', 'Delhi NCR', 'https://upload.wikimedia.org/wikipedia/commons/f/fb/Bharti_Airtel_Logo.svg'),
(3, 'Vi (Vodafone Idea)', 'Prepaid', 'Mumbai', 'https://upload.wikimedia.org/wikipedia/commons/2/25/Vodafone_Idea_logo.svg'),
(4, 'BSNL', 'Prepaid', 'All India', 'https://upload.wikimedia.org/wikipedia/commons/b/b3/BSNL_logo.svg'),
(5, 'Symphony 5G', 'Prepaid', 'Premium Network', 'https://img.icons8.com/fluency/96/lightning-bolt.png');

-- 2. Insert Plans for Jio (ID: 1)
INSERT INTO plans (operator_id, amount, validity, description) VALUES
(1, 299.00, '28 Days', 'Hero Unlimited: 1.5GB/Day + True 5G Unlimited + JioTV'),
(1, 199.00, '23 Days', 'Daily Saver: 1.5GB/Day + Unlimited Calls'),
(1, 666.00, '84 Days', 'Quarterly Value: 1.5GB/Day + JioCinema Premium'),
(1, 2999.00, '365 Days', 'Annual Premium: 2.5GB/Day + Prime Video + Disney+ Hotstar'),
(1, 19.00, '1 Day', 'Data Booster: 1GB High Speed Data'),
(1, 100.00, 'Unlimited', 'Talktime: ₹81.75 balance credit');

-- 3. Insert Plans for Airtel (ID: 2)
INSERT INTO plans (operator_id, amount, validity, description) VALUES
(2, 299.00, '28 Days', 'Truly Unlimited: 1.5GB/Day + Xstream Play + Apollo 24/7'),
(2, 479.00, '56 Days', 'Balanced Choice: 1.5GB/Day + Wynk Music Free'),
(2, 3359.00, '365 Days', 'Yearly Mega: 2.5GB/Day + Disney+ Hotstar + Amazon Prime'),
(2, 58.00, 'Base Plan', 'Data Add-on: 3GB Data for current validity'),
(2, 99.00, '28 Days', 'Smart Recharge: ₹99 Talktime + 200MB Data');

-- 4. Insert Plans for Symphony 5G (ID: 5)
INSERT INTO plans (operator_id, amount, validity, description) VALUES
(5, 499.00, '30 Days', 'Symphony Gold: 3GB/Day + High Fidelity Audio Streaming'),
(5, 899.00, '60 Days', 'Symphony Platinum: 5GB/Day + Premium Concierge Access'),
(5, 99.00, '7 Days', 'Symphony Trial: 2GB/Day + Ad-free Experience');

-- 5. Insert Dummy Users (rechargehub_users database)
USE rechargerhub_users;
INSERT IGNORE INTO users2 (id, name, email, password, role, phone_number, is_verified, created_at) VALUES
(2, 'Sarah Jenkins', 'sarah@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHHi', 'ROLE_USER', '9876543210', true, NOW()),
(3, 'Michael Ross', 'mike@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHHi', 'ROLE_USER', '8765432109', true, NOW()),
(4, 'Harvey Specter', 'harvey@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHHi', 'ROLE_USER', '7654321098', true, NOW());

-- 6. Insert Dummy Transactions (rechargehub_payments database)
USE rechargehub_payments;
INSERT INTO transactions (id, user_id, amount, status, payment_method, transaction_id, order_id, mobile_number, created_at) VALUES
(1, 2, 299.00, 'SUCCESS', 'UPI', 'pay_SYMP12345', 'order_123', '9876543210', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 2, 19.00, 'SUCCESS', 'WALLET', 'pay_SYMP12346', 'order_124', '9876543210', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(3, 3, 499.00, 'SUCCESS', 'CARD', 'pay_SYMP12347', 'order_125', '8765432109', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 4, 3359.00, 'SUCCESS', 'NETBANKING', 'pay_SYMP12348', 'order_126', '7654321098', DATE_SUB(NOW(), INTERVAL 10 MINUTE));

-- 7. Insert Recharge Records (rechargehub_recharges database)
USE rechargehub_recharges;
INSERT INTO recharges (user_id, operator_id, plan_id, amount, status, transaction_id, mobile_number, created_at) VALUES
(2, 1, 1, 299.00, 'SUCCESS', 'pay_SYMP12345', '9876543210', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 2, 1, 299.00, 'SUCCESS', 'pay_SYMP12347', '8765432109', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 5, 1, 499.00, 'SUCCESS', 'pay_SYMP12348', '7654321098', DATE_SUB(NOW(), INTERVAL 10 MINUTE));
