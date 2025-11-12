-- =====================================================
-- Simple SQL Script to Insert Sample Users and Groups
-- Run this against camundadb database
-- =====================================================

USE camundadb;

-- Insert Groups
INSERT INTO ACT_ID_GROUP (ID_, REV_, NAME_, TYPE_) VALUES
('order_managers', 1, 'Order Managers', 'GROUP'),
('finance_team', 1, 'Finance Team', 'GROUP'),
('warehouse_team', 1, 'Warehouse Team', 'GROUP'),
('delivery_team', 1, 'Delivery Team', 'GROUP')
ON DUPLICATE KEY UPDATE NAME_ = VALUES(NAME_);

-- Insert Users (passwords are MD5 hashed - password = username)
INSERT INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_) VALUES
('manager1', 1, 'John', 'Manager', 'john.manager@example.com', MD5('manager1')),
('manager2', 1, 'Sarah', 'Manager', 'sarah.manager@example.com', MD5('manager2')),
('finance1', 1, 'Michael', 'Finance', 'finance1@example.com', MD5('finance1')),
('finance2', 1, 'Emily', 'Finance', 'emily.finance@example.com', MD5('finance2')),
('warehouse1', 1, 'Mike', 'Warehouse', 'mike.warehouse@example.com', MD5('warehouse1')),
('warehouse2', 1, 'Lisa', 'Warehouse', 'lisa.warehouse@example.com', MD5('warehouse2')),
('delivery1', 1, 'David', 'Delivery', 'david.delivery@example.com', MD5('delivery1')),
('delivery2', 1, 'Anna', 'Delivery', 'anna.delivery@example.com', MD5('delivery2'))
ON DUPLICATE KEY UPDATE FIRST_ = VALUES(FIRST_), LAST_ = VALUES(LAST_), EMAIL_ = VALUES(EMAIL_);

-- Insert Memberships (User-Group relationships)
INSERT IGNORE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_) VALUES
('manager1', 'order_managers'),
('manager2', 'order_managers'),
('finance1', 'finance_team'),
('finance2', 'finance_team'),
('warehouse1', 'warehouse_team'),
('warehouse2', 'warehouse_team'),
('delivery1', 'delivery_team'),
('delivery2', 'delivery_team');

-- Verify data
SELECT 'Groups:' AS Info;
SELECT * FROM ACT_ID_GROUP;

SELECT 'Users:' AS Info;
SELECT ID_, FIRST_, LAST_, EMAIL_ FROM ACT_ID_USER;

SELECT 'Memberships:' AS Info;
SELECT * FROM ACT_ID_MEMBERSHIP;

