# Order Created Event Propagation Flow

This document explains how the **Order Created Event** propagates through the system and what gets stored in the Camunda database (`camundadb`).

## Overview

When an order is created, the event flows through multiple systems:
1. **Order Service** - Creates and stores the order
2. **Kafka** - Publishes the event to a topic
3. **Camunda Service** - Consumes the event and starts a workflow process
4. **Notification Service** - Consumes the event and sends notifications

---

## Event Flow Diagram

```
┌─────────────────┐
│  Order Service  │
│  (Port 6060)    │
└────────┬────────┘
         │
         │ 1. Create Order
         │    - Save to orderdb
         │    - Clear cart
         │
         │ 2. Publish Event
         ▼
┌─────────────────┐
│  Kafka Topic    │
│ orders.created  │
└────────┬────────┘
         │
         ├─────────────────┬─────────────────┐
         │                 │                 │
         ▼                 ▼                 ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ Camunda Service │ │Notification Svc│ │  (Future)       │
│  (Port 5050)    │ │  (Port 8080)   │ │  Other Services │
└────────┬────────┘ └─────────────────┘ └─────────────────┘
         │
         │ 3. Start Process Instance
         ▼
┌─────────────────┐
│  camundadb      │
│  (MySQL)        │
└─────────────────┘
```

---

## Step-by-Step Propagation

### Step 1: Order Creation (Order Service)

**Location:** `order-service/src/main/java/com/layoff/order_service/services/OrderService.java`

**What Happens:**
1. Order is created from cart items
2. Order is **saved to `orderdb` database** (MySQL)
   - Table: `orders`
   - Table: `order_items`
3. Cart is cleared
4. `OrderCreatedEvent` is created with order details
5. Event is published to Kafka topic `orders.created`

**Code Reference:**
```java
Order savedOrder = orderRepository.save(order);  // Stored in orderdb
streamBridge.send("orderCreated-out-0", event);  // Published to Kafka
```

**Event Structure:**
```java
OrderCreatedEvent {
    orderId: Long
    userId: String
    status: OrderStatus
    items: List<OrderItemDTO>
    totalAmount: BigDecimal
    createdAt: LocalDateTime
}
```

---

### Step 2: Kafka Message Broker

**Topic:** `orders.created`

**Configuration:**
- **Producer:** `order-service` (binding: `orderCreated-out-0`)
- **Consumers:**
  - `camunda-service` (group: `camunda-service`)
  - `notification-service` (group: `notification-service`)

**Location:** `configserver/src/main/resources/config/`

**Properties:**
- `order-service.properties`: `spring.cloud.stream.bindings.orderCreated-out-0.destination=orders.created`
- `camunda-service.properties`: `spring.cloud.stream.bindings.orderCreated-in-0.destination=orders.created`
- `notification-service.properties`: `spring.cloud.stream.bindings.orderCreated-in-0.destination=orders.created`

---

### Step 3: Camunda Service Consumption

**Location:** `camunda-service/src/main/java/com/layoff/camunda_service/CamundaServiceApplication.java`

**What Happens:**
1. Camunda service consumes the `OrderCreatedEvent` from Kafka
2. Extracts process variables:
   - `orderId`
   - `userId`
   - `totalAmount`
3. Starts a new Camunda process instance using `order_process` BPMN definition

**Code:**
```java
@Bean
public Consumer<OrderCreatedEvent> orderCreated(RuntimeService runtimeService) {
    return event -> {
        Map<String, Object> vars = new HashMap<>();
        vars.put("orderId", event.orderId());
        vars.put("userId", event.userId());
        vars.put("totalAmount", event.totalAmount());
        runtimeService.startProcessInstanceByKey("order_process", vars);
    };
}
```

---

### Step 4: Storage in Camunda Database

**Database:** `camundadb` (MySQL on `localhost:3306`)

**What Gets Stored:**

When `runtimeService.startProcessInstanceByKey()` is called, Camunda automatically stores:

#### 1. Process Instance
- **Table:** `ACT_RU_EXECUTION` (Runtime Executions)
- **Table:** `ACT_RU_PROCESS_INSTANCE` (Process Instances)
- Contains: Process instance ID, business key, process definition key

#### 2. Process Variables
- **Table:** `ACT_RU_VARIABLE` (Runtime Variables)
- Stores:
  - `orderId` (Long)
  - `userId` (String)
  - `totalAmount` (BigDecimal)

#### 3. Process Definition
- **Table:** `ACT_RE_PROCDEF` (Process Definitions)
- Contains: BPMN process definition (`order_process`)

#### 4. History (if history level is enabled)
- **Table:** `ACT_HI_PROCINST` (History Process Instances)
- **Table:** `ACT_HI_VARINST` (History Variables)
- **Table:** `ACT_HI_ACTINST` (History Activity Instances)
- **Retention:** 30 days (configured in BPMN: `camunda:historyTimeToLive="P30D"`)

#### 5. Tasks
- **Table:** `ACT_RU_TASK` (Runtime Tasks)
- When process reaches `userTaskReview`, a task is created
- Assigned to candidate group: `order-managers`

---

## BPMN Process Definition

**Location:** `camunda-service/src/main/resources/processes/order-process.bpmn`

**Process Flow:**
1. **Start Event** → "Order Received"
2. **User Task** → "Review Order" (candidate group: `order-managers`)
3. **End Event** → "Completed"

**Process ID:** `order_process`

---

## Database Schemas

### Order Database (`orderdb`)

**Tables:**
- `orders`
  - `id` (PK)
  - `user_id`
  - `status`
  - `total_amount`
  - `created_at`
  - `updated_at`
- `order_items`
  - `id` (PK)
  - `order_id` (FK)
  - `product_id`
  - `quantity`
  - `price`

### Camunda Database (`camundadb`)

**Key Tables:**
- `ACT_RU_EXECUTION` - Active process executions
- `ACT_RU_PROCESS_INSTANCE` - Active process instances
- `ACT_RU_VARIABLE` - Process variables (orderId, userId, totalAmount)
- `ACT_RU_TASK` - Active user tasks
- `ACT_RE_PROCDEF` - Process definitions
- `ACT_HI_PROCINST` - Historical process instances
- `ACT_HI_VARINST` - Historical variables
- `ACT_HI_ACTINST` - Historical activity instances

---

## Verification Queries

### Check Order in orderdb
```sql
USE orderdb;
SELECT * FROM orders ORDER BY created_at DESC LIMIT 10;
SELECT * FROM order_items WHERE order_id = <orderId>;
```

### Check Process Instance in camundadb
```sql
USE camundadb;

-- Find process instances
SELECT * FROM ACT_RU_PROCESS_INSTANCE 
WHERE PROC_DEF_KEY_ = 'order_process' 
ORDER BY START_TIME_ DESC;

-- Find process variables
SELECT * FROM ACT_RU_VARIABLE 
WHERE PROC_INST_ID_ = <processInstanceId>;

-- Find tasks
SELECT * FROM ACT_RU_TASK 
WHERE PROC_INST_ID_ = <processInstanceId>;
```

### Check Kafka Messages
```bash
# View messages in topic
docker exec -it commercial_site-kafka-1 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic orders.created \
  --from-beginning

# Check consumer groups
docker exec commercial_site-kafka-1 kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe --group camunda-service
```

---

## Summary

### ✅ What Gets Stored:

1. **Order Data** → Stored in `orderdb` (orders and order_items tables)
2. **Process Instance** → Stored in `camundadb` (ACT_RU_* tables)
3. **Process Variables** → Stored in `camundadb` (ACT_RU_VARIABLE table)
4. **Process History** → Stored in `camundadb` (ACT_HI_* tables) for 30 days
5. **User Tasks** → Stored in `camundadb` (ACT_RU_TASK table)

### ❌ What Does NOT Happen:

- **No .md file is automatically generated** - The event does not create markdown files
- The event is ephemeral in Kafka (unless configured for retention)
- The event itself is not stored, only the resulting process instance data

### 📝 Documentation Files:

- This file (`ORDER_EVENT_PROPAGATION.md`) - Manual documentation
- `KAFKA_COMMANDS.md` - Kafka command reference
- `POSTMAN_GUIDE.md` - API testing guide
- `EMAIL_SETUP_GUIDE.md` - Email configuration

---

## Configuration Files

- **Order Service:** `configserver/src/main/resources/config/order-service.properties`
- **Camunda Service:** `configserver/src/main/resources/config/camunda-service.properties`
- **Notification Service:** `configserver/src/main/resources/config/notification-service.properties`

---

## Related Services

1. **Order Service** (Port 6060) - Creates orders and publishes events
2. **Camunda Service** (Port 5050) - Consumes events and manages workflows
3. **Notification Service** (Port 8080) - Sends email notifications
4. **Kafka** (Port 9092) - Message broker
5. **MySQL** (Port 3306) - Databases (orderdb, camundadb)

---

## Troubleshooting

### Event Not Reaching Camunda
1. Check Kafka topic exists: `docker exec commercial_site-kafka-1 kafka-topics --bootstrap-server localhost:9092 --list`
2. Check consumer group: `docker exec commercial_site-kafka-1 kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group camunda-service`
3. Check Camunda service logs for errors

### Process Instance Not Created
1. Verify BPMN file is deployed: Check Camunda Cockpit UI
2. Check `camundadb` connection in `camunda-service.properties`
3. Verify process definition key matches: `order_process`

### Variables Not Stored
1. Check variable names match in code and BPMN
2. Verify variable types are supported by Camunda
3. Check `ACT_RU_VARIABLE` table in `camundadb`

---

**Last Updated:** Generated automatically based on current codebase structure


