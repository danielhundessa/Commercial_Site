# SQL Scripts for Camunda Identity Data

This directory contains SQL scripts to manually insert users and groups into the Camunda identity tables.

## Database Connection

- **Database**: `camundadb`
- **Host**: `localhost:3306`
- **Username**: `springstudent`
- **Password**: `springstudent`

## Files

1. **`insert-sample-users-groups.sql`** - Detailed script with comments and verification queries
2. **`insert-sample-users-groups-simple.sql`** - Simple, concise script

## How to Run

### Option 1: Using MySQL Command Line

```bash
# Connect to MySQL
mysql -u springstudent -p springstudent

# Run the script
USE camundadb;
SOURCE camunda-service/src/main/resources/db/insert-sample-users-groups-simple.sql;
```

### Option 2: Using MySQL Workbench or DBeaver

1. Connect to your MySQL database
2. Select the `camundadb` database
3. Open the SQL script file
4. Execute the script

### Option 3: Using Command Line (One-liner)

```bash
mysql -u springstudent -pspringstudent camundadb < camunda-service/src/main/resources/db/insert-sample-users-groups-simple.sql
```

## What Gets Inserted

### Groups (4 groups)
- `order_managers` - Order Managers
- `finance_team` - Finance Team
- `warehouse_team` - Warehouse Team
- `delivery_team` - Delivery Team

### Users (8 users)
- **Order Managers**: `manager1`, `manager2`
- **Finance Team**: `finance1`, `finance2`
- **Warehouse Team**: `warehouse1`, `warehouse2`
- **Delivery Team**: `delivery1`, `delivery2`

### Passwords
All users have password = username (e.g., `manager1`/`manager1`)
Passwords are stored as MD5 hashes in the database.

## Tables Used

- **ACT_ID_GROUP** - Stores groups
- **ACT_ID_USER** - Stores users
- **ACT_ID_MEMBERSHIP** - Stores user-group relationships

## Verification

After running the script, verify the data:

```sql
-- Check groups
SELECT * FROM ACT_ID_GROUP;

-- Check users
SELECT ID_, FIRST_, LAST_, EMAIL_ FROM ACT_ID_USER;

-- Check memberships
SELECT m.USER_ID_, m.GROUP_ID_, u.FIRST_, u.LAST_, g.NAME_
FROM ACT_ID_MEMBERSHIP m
LEFT JOIN ACT_ID_USER u ON m.USER_ID_ = u.ID_
LEFT JOIN ACT_ID_GROUP g ON m.GROUP_ID_ = g.ID_
ORDER BY m.GROUP_ID_;
```

## Notes

- The scripts use `ON DUPLICATE KEY UPDATE` for groups and users to prevent errors if run multiple times
- The scripts use `INSERT IGNORE` for memberships to prevent duplicate key errors
- Passwords are hashed using MySQL's `MD5()` function (Camunda's default)


