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

### 2. Start Docker Services (Kafka, Zookeeper, MailHog)

```bash
# Start all Docker services (Kafka, Zookeeper, MailHog)
docker-compose up -d

# Verify services are running
docker-compose ps

# View Kafka logs
docker-compose logs -f kafka

# View MailHog logs
docker-compose logs -f mailhog
```

**Services Started:**
- **Kafka**: `localhost:9092` (Message broker)
- **Zookeeper**: `localhost:2181` (Kafka dependency)
- **MailHog**: 
  - SMTP: `localhost:1025` (for sending emails)
  - Web UI: `http://localhost:8025` (to view sent emails)

**Note:** The `create-topics` service automatically creates the `orders.created` Kafka topic when Kafka is healthy.

### 3. Start Application Services

```bash
# Start Camunda Service (port 5050)
cd camunda-service
mvn spring-boot:run

# In separate terminals, start other services:
# - Order Service (port 6060)
# - Notification Service (port 7075)
# - Config Server (port 8888)
# - Eureka Server (port 8761)
```

### 4. Verify Tables Created

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

### 5. Start React Frontend

```bash
cd camunda-frontend
npm install
npm start
```

Open: http://localhost:3000

### 6. Test the Flow

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

- `order_managers` - Review Order
- `finance_team` - Approve Payment
- `warehouse_team` - Prepare Shipping
- `delivery_team` - Confirm Delivery

For detailed information, see `CAMUNDA_INTEGRATION_GUIDE.md`




