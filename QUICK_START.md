# Quick Start Guide - Camunda Integration

## Problem Fixed

✅ **Camunda tables not being created** - Fixed database configuration
✅ **No process instances** - Process now starts automatically when orders are created
✅ **No React frontend** - Complete React application added

## Quick Setup

### 1. Database Setup

```sql
CREATE DATABASE camundadb;
```

### 2. Start Services

```bash
# Start Kafka
docker-compose up -d

# Start Camunda Service (port 5050)
cd camunda-service
mvn spring-boot:run
```

### 3. Verify Tables Created

```sql
USE camundadb;
SHOW TABLES LIKE 'ACT_%';
```

You should see tables like:
- ACT_RU_PROCESS_INSTANCE
- ACT_RU_VARIABLE
- ACT_RU_TASK
- ACT_HI_PROCINST
- etc.

### 4. Start React Frontend

```bash
cd camunda-frontend
npm install
npm start
```

Open: http://localhost:3000

### 5. Test the Flow

1. **Create Order** via Postman:
   ```
   POST http://localhost:8080/api/orders
   Header: X-User-ID: user123
   ```

2. **Check Process Instance**:
   ```sql
   SELECT * FROM ACT_RU_PROCESS_INSTANCE;
   SELECT * FROM ACT_RU_VARIABLE;
   SELECT * FROM ACT_RU_TASK;
   ```

3. **Complete Tasks** via React app:
   - Open http://localhost:3000
   - Filter by candidate group
   - Claim and complete tasks

## Key Files

- **BPMN Process**: `camunda-service/src/main/resources/processes/order-process.bpmn`
- **REST API**: `camunda-service/src/main/java/.../controllers/TaskController.java`
- **React App**: `camunda-frontend/`
- **Documentation**: `CAMUNDA_INTEGRATION_GUIDE.md`

## Process Flow

Order → Validate → Review → Payment Approval → Process Payment → Shipping → Delivery → Complete

## User Task Groups

- `order-managers` - Review Order
- `finance-team` - Approve Payment
- `warehouse-team` - Prepare Shipping
- `delivery-team` - Confirm Delivery

For detailed information, see `CAMUNDA_INTEGRATION_GUIDE.md`



