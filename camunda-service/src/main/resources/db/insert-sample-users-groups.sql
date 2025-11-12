-- =====================================================
-- SQL Script to Insert Sample Users and Groups
-- Camunda Identity Tables: ACT_ID_USER, ACT_ID_GROUP, ACT_ID_MEMBERSHIP
-- =====================================================
-- Usage: Run this script against the camundadb database
-- =====================================================

USE camundadb;

-- =====================================================
-- 1. INSERT GROUPS
-- =====================================================
-- Note: REV_ is revision number (starts at 1)
--       TYPE_ is typically "GROUP" for user groups

INSERT INTO ACT_ID_GROUP (ID_, REV_, NAME_, TYPE_) 
VALUES ('order_managers', 1, 'Order Managers', 'GROUP')
ON DUPLICATE KEY UPDATE NAME_ = 'Order Managers';

INSERT INTO ACT_ID_GROUP (ID_, REV_, NAME_, TYPE_) 
VALUES ('finance_team', 1, 'Finance Team', 'GROUP')
ON DUPLICATE KEY UPDATE NAME_ = 'Finance Team';

INSERT INTO ACT_ID_GROUP (ID_, REV_, NAME_, TYPE_) 
VALUES ('warehouse_team', 1, 'Warehouse Team', 'GROUP')
ON DUPLICATE KEY UPDATE NAME_ = 'Warehouse Team';

INSERT INTO ACT_ID_GROUP (ID_, REV_, NAME_, TYPE_) 
VALUES ('delivery_team', 1, 'Delivery Team', 'GROUP')
ON DUPLICATE KEY UPDATE NAME_ = 'Delivery Team';

-- =====================================================
-- 2. INSERT USERS
-- =====================================================
-- Note: PWD_ stores MD5 hashed passwords
--       Passwords are hashed using MD5 (Camunda default)
--       All sample users have password = username (e.g., manager1/manager1)

-- Order Managers
INSERT INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_) 
VALUES ('manager1', 1, 'John', 'Manager', 'john.manager@example.com', MD5('manager1'))
ON DUPLICATE KEY UPDATE FIRST_ = 'John', LAST_ = 'Manager', EMAIL_ = 'john.manager@example.com';

INSERT INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_) 
VALUES ('manager2', 1, 'Sarah', 'Manager', 'sarah.manager@example.com', MD5('manager2'))
ON DUPLICATE KEY UPDATE FIRST_ = 'Sarah', LAST_ = 'Manager', EMAIL_ = 'sarah.manager@example.com';

-- Finance Team
INSERT INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_) 
VALUES ('finance1', 1, 'Michael', 'Finance', 'finance1@example.com', MD5('finance1'))
ON DUPLICATE KEY UPDATE FIRST_ = 'Michael', LAST_ = 'Finance', EMAIL_ = 'finance1@example.com';

INSERT INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_) 
VALUES ('finance2', 1, 'Emily', 'Finance', 'emily.finance@example.com', MD5('finance2'))
ON DUPLICATE KEY UPDATE FIRST_ = 'Emily', LAST_ = 'Finance', EMAIL_ = 'emily.finance@example.com';

-- Warehouse Team
INSERT INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_) 
VALUES ('warehouse1', 1, 'Mike', 'Warehouse', 'mike.warehouse@example.com', MD5('warehouse1'))
ON DUPLICATE KEY UPDATE FIRST_ = 'Mike', LAST_ = 'Warehouse', EMAIL_ = 'mike.warehouse@example.com';

INSERT INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_) 
VALUES ('warehouse2', 1, 'Lisa', 'Warehouse', 'lisa.warehouse@example.com', MD5('warehouse2'))
ON DUPLICATE KEY UPDATE FIRST_ = 'Lisa', LAST_ = 'Warehouse', EMAIL_ = 'lisa.warehouse@example.com';

-- Delivery Team
INSERT INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_) 
VALUES ('delivery1', 1, 'David', 'Delivery', 'david.delivery@example.com', MD5('delivery1'))
ON DUPLICATE KEY UPDATE FIRST_ = 'David', LAST_ = 'Delivery', EMAIL_ = 'david.delivery@example.com';

INSERT INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_) 
VALUES ('delivery2', 1, 'Anna', 'Delivery', 'anna.delivery@example.com', MD5('delivery2'))
ON DUPLICATE KEY UPDATE FIRST_ = 'Anna', LAST_ = 'Delivery', EMAIL_ = 'anna.delivery@example.com';

-- =====================================================
-- 3. INSERT MEMBERSHIPS (User-Group Relationships)
-- =====================================================

-- Order Managers Group
INSERT IGNORE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_) 
VALUES ('manager1', 'order_managers');

INSERT IGNORE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_) 
VALUES ('manager2', 'order_managers');

-- Finance Team Group
INSERT IGNORE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_) 
VALUES ('finance1', 'finance_team');

INSERT IGNORE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_) 
VALUES ('finance2', 'finance_team');

-- Warehouse Team Group
INSERT IGNORE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_) 
VALUES ('warehouse1', 'warehouse_team');

INSERT IGNORE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_) 
VALUES ('warehouse2', 'warehouse_team');

-- Delivery Team Group
INSERT IGNORE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_) 
VALUES ('delivery1', 'delivery_team');

INSERT IGNORE INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_) 
VALUES ('delivery2', 'delivery_team');

-- =====================================================
-- 4. VERIFICATION QUERIES
-- =====================================================

-- View all groups
SELECT '=== GROUPS ===' AS Info;
SELECT ID_, NAME_, TYPE_ FROM ACT_ID_GROUP ORDER BY ID_;

-- View all users
SELECT '=== USERS ===' AS Info;
SELECT ID_, FIRST_, LAST_, EMAIL_ FROM ACT_ID_USER ORDER BY ID_;

-- View all memberships
SELECT '=== MEMBERSHIPS ===' AS Info;
SELECT m.USER_ID_, u.FIRST_, u.LAST_, m.GROUP_ID_, g.NAME_ 
FROM ACT_ID_MEMBERSHIP m
LEFT JOIN ACT_ID_USER u ON m.USER_ID_ = u.ID_
LEFT JOIN ACT_ID_GROUP g ON m.GROUP_ID_ = g.ID_
ORDER BY m.GROUP_ID_, m.USER_ID_;

-- View users by group
SELECT '=== USERS BY GROUP ===' AS Info;
SELECT g.ID_ AS GroupID, g.NAME_ AS GroupName, 
       GROUP_CONCAT(u.ID_ ORDER BY u.ID_ SEPARATOR ', ') AS Users
FROM ACT_ID_GROUP g
LEFT JOIN ACT_ID_MEMBERSHIP m ON g.ID_ = m.GROUP_ID_
LEFT JOIN ACT_ID_USER u ON m.USER_ID_ = u.ID_
GROUP BY g.ID_, g.NAME_
ORDER BY g.ID_;

-- =====================================================
-- END OF SCRIPT
-- =====================================================

