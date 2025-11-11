# Camunda Integration Guide

## Table of Contents
1. [Overview](#overview)
2. [What Was Done](#what-was-done)
3. [Process Flow](#process-flow)
4. [Camunda Database Tables](#camunda-database-tables)
5. [Setup Instructions](#setup-instructions)
6. [Running the React Project](#running-the-react-project)
7. [Testing Camunda](#testing-camunda)
8. [Camunda Modeler Integration](#camunda-modeler-integration)
9. [Troubleshooting](#troubleshooting)

---

## Overview

This guide explains the complete Camunda workflow integration for the e-commerce order processing system. The integration includes:

- **Backend**: Spring Boot service with Camunda BPM engine
- **Frontend**: React application for task management
- **Process**: Comprehensive order processing workflow with multiple user tasks

---

## What Was Done

### 1. Fixed Camunda Database Configuration

**Problem**: Camunda tables (ACT_RU_PROCESS_INSTANCE, ACT_RU_VARIABLE, etc.) were not being created.

**Solution**: 
- Added proper Camunda database configuration in `camunda-service.properties`:
  - `camunda.bpm.database.type=mysql`
  - `camunda.bpm.database.schema-update=true`
  - `camunda.bpm.history-level=full`

**Files Modified**:
- `configserver/src/main/resources/config/camunda-service.properties`

### 2. Created Comprehensive BPMN Process

**File**: `camunda-service/src/main/resources/processes/order-process.bpmn`

**Process Flow**:
1. **Start Event**: Order Received
2. **Service Task**: Validate Order (automatic)
3. **User Task**: Review Order (order-managers group)
4. **Gateway**: Review Decision (Approved/Rejected)
5. **User Task**: Approve Payment (finance-team group)
6. **Gateway**: Payment Decision (Approved/Rejected)
7. **Service Task**: Process Payment (automatic)
8. **User Task**: Prepare Shipping (warehouse-team group)
9. **Service Task**: Ship Order (automatic)
10. **User Task**: Confirm Delivery (delivery-team group)
11. **End Event**: Order Completed

### 3. Created Java Delegates

**Service Task Implementations**:
- `OrderValidationDelegate`: Validates incoming orders
- `PaymentProcessingDelegate`: Processes payments
- `ShippingDelegate`: Handles shipping operations

**Location**: `camunda-service/src/main/java/com/layoff/camunda_service/delegates/`

### 4. Created REST API Endpoints

**Task Management**:
- `GET /api/camunda/tasks` - Get all tasks (with optional filters)
- `GET /api/camunda/tasks/{taskId}` - Get specific task
- `GET /api/camunda/tasks/{taskId}/variables` - Get task variables
- `POST /api/camunda/tasks/{taskId}/claim` - Claim a task
- `POST /api/camunda/tasks/{taskId}/complete` - Complete a task
- `POST /api/camunda/tasks/{taskId}/unclaim` - Unclaim a task

**Process Instance Management**:
- `GET /api/camunda/process-instances` - Get all process instances
- `GET /api/camunda/process-instances/{processInstanceId}` - Get specific instance
- `GET /api/camunda/process-instances/{processInstanceId}/variables` - Get instance variables

**Location**: `camunda-service/src/main/java/com/layoff/camunda_service/controllers/`

### 5. Created React Frontend

**Features**:
- Task list with filtering by candidate group
- Task detail view with form inputs
- Task claiming/unclaiming
- Task completion with variable updates
- Process instance viewing
- Process variable inspection

**Location**: `camunda-frontend/`

---

## Process Flow

### Order Processing Workflow

```
┌─────────────────┐
│ Order Received  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Validate Order  │ (Service Task - Automatic)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Review Order   │ (User Task - order-managers)
└────────┬────────┘
         │
         ▼
    ┌────────┐
    │Gateway │ (Review Decision)
    └───┬────┘
        │
    ┌───┴────┐
    │        │
  Approved  Rejected
    │        │
    │        └──► [End: Order Rejected]
    │
    ▼
┌─────────────────┐
│ Approve Payment │ (User Task - finance-team)
└────────┬────────┘
         │
         ▼
    ┌────────┐
    │Gateway │ (Payment Decision)
    └───┬────┘
        │
    ┌───┴────┐
    │        │
  Approved  Rejected
    │        │
    │        └──► [End: Payment Rejected]
    │
    ▼
┌─────────────────┐
│ Process Payment │ (Service Task - Automatic)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│Prepare Shipping │ (User Task - warehouse-team)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Ship Order    │ (Service Task - Automatic)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│Confirm Delivery │ (User Task - delivery-team)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│Order Completed  │
└─────────────────┘
```

### Process Variables

The following variables are used throughout the process:

- `orderId`: Long - The order ID
- `userId`: String - The user who placed the order
- `totalAmount`: BigDecimal - Total order amount
- `orderValidated`: Boolean - Order validation result
- `reviewApproved`: Boolean - Review approval decision
- `reviewComments`: String - Review comments
- `paymentApproved`: Boolean - Payment approval decision
- `paymentComments`: String - Payment comments
- `paymentId`: String - Generated payment ID
- `paymentProcessed`: Boolean - Payment processing status
- `trackingNumber`: String - Shipping tracking number
- `orderShipped`: Boolean - Shipping status
- `deliveryConfirmed`: Boolean - Delivery confirmation
- `deliveryNotes`: String - Delivery notes

---

## Camunda Database Tables

When Camunda starts, it automatically creates the following tables in the `camundadb` database:

### Runtime Tables (ACT_RU_*)

These tables store active/running process instances:

1. **ACT_RU_PROCESS_INSTANCE**
   - Stores active process instances
   - Columns: ID_, REV_, PROC_INST_ID_, BUSINESS_KEY_, PROC_DEF_ID_, etc.
   - **What gets stored**: Each time a process is started via `runtimeService.startProcessInstanceByKey()`, a new row is created here.

2. **ACT_RU_VARIABLE**
   - Stores process variables for active instances
   - Columns: ID_, REV_, TYPE_, NAME_, PROC_INST_ID_, EXECUTION_ID_, etc.
   - **What gets stored**: All variables set during process execution (orderId, userId, totalAmount, reviewApproved, etc.)

3. **ACT_RU_TASK**
   - Stores active user tasks
   - Columns: ID_, REV_, NAME_, ASSIGNEE_, PROC_INST_ID_, TASK_DEF_KEY_, etc.
   - **What gets stored**: Each user task (Review Order, Approve Payment, etc.) creates a row here.

4. **ACT_RU_EXECUTION**
   - Stores execution instances (paths through the process)
   - **What gets stored**: Execution paths and branches in the process

5. **ACT_RU_IDENTITYLINK**
   - Stores task assignments and candidate groups
   - **What gets stored**: Task assignments, candidate users, candidate groups

6. **ACT_RU_JOB**
   - Stores asynchronous jobs (timers, async service tasks)
   - **What gets stored**: Jobs for async service tasks

### History Tables (ACT_HI_*)

These tables store historical data (with history level = FULL):

1. **ACT_HI_PROCINST**
   - Historical process instances (completed and active)
   - **What gets stored**: All process instances with their start/end times

2. **ACT_HI_VARINST**
   - Historical variable values
   - **What gets stored**: All variable values at different points in time

3. **ACT_HI_TASKINST**
   - Historical task instances
   - **What gets stored**: All tasks (completed and active) with their details

4. **ACT_HI_ACTINST**
   - Historical activity instances
   - **What gets stored**: Every activity (task, gateway, service task) execution

5. **ACT_HI_DETAIL**
   - Detailed history information
   - **What gets stored**: Variable updates, form submissions, etc.

### Definition Tables (ACT_RE_*)

1. **ACT_RE_DEPLOYMENT**
   - Stores BPMN process deployments
   - **What gets stored**: Each time a BPMN file is deployed

2. **ACT_RE_PROCDEF**
   - Stores process definitions
   - **What gets stored**: Process definitions from deployed BPMN files

### Identity Tables (ACT_ID_*)

1. **ACT_ID_USER**
   - User accounts
2. **ACT_ID_GROUP**
   - User groups
3. **ACT_ID_MEMBERSHIP**
   - User-group memberships

---

## Setup Instructions

### Prerequisites

1. **MySQL Database**
   - Create database: `camundadb`
   - User: `springstudent`
   - Password: `springstudent`

2. **Kafka** (for event streaming)
   - Running on `localhost:9092`

3. **Java 17+**
4. **Node.js 16+** (for React frontend)
5. **Maven 3.6+**

### Step 1: Database Setup

```sql
CREATE DATABASE camundadb;
CREATE USER 'springstudent'@'localhost' IDENTIFIED BY 'springstudent';
GRANT ALL PRIVILEGES ON camundadb.* TO 'springstudent'@'localhost';
FLUSH PRIVILEGES;
```

### Step 2: Start Infrastructure Services

```bash
# Start Kafka and Zookeeper
docker-compose up -d
```

### Step 3: Start Backend Services

Start services in this order:

1. **Eureka Server** (port 8761)
2. **Config Server** (port 8888)
3. **Camunda Service** (port 5050)
4. **Order Service** (port varies)
5. **Gateway Service** (port varies)

### Step 4: Verify Camunda Tables

After starting the Camunda service, check the database:

```sql
USE camundadb;
SHOW TABLES LIKE 'ACT_%';
```

You should see all ACT_* tables created.

### Step 5: Verify Process Deployment

Check the Camunda REST API:

```bash
curl http://localhost:5050/engine-rest/process-definition
```

You should see the `order_process` definition.

---

## Running the React Project

### Step 1: Navigate to Frontend Directory

```bash
cd camunda-frontend
```

### Step 2: Install Dependencies

```bash
npm install
```

### Step 3: Configure API URL (Optional)

Create `.env` file:

```
REACT_APP_CAMUNDA_API_URL=http://localhost:5050/api/camunda
```

### Step 4: Start the Application

```bash
npm start
```

The application will open at `http://localhost:3000`

### Step 5: Using the Frontend

1. **View Tasks**: Navigate to the Tasks page
2. **Filter Tasks**: Select a candidate group (order-managers, finance-team, etc.)
3. **Claim Tasks**: Enter a user ID and click "Claim" on unassigned tasks
4. **Complete Tasks**: Click on a task to view details and complete it
5. **View Process Instances**: Navigate to Process Instances page

---

## Testing Camunda

### Test 1: Create an Order via Postman

**Endpoint**: `POST http://localhost:8080/api/orders`
**Headers**: 
```
X-User-ID: user123
```

**Expected Result**:
1. Order is created in `orderdb`
2. OrderCreated event is published to Kafka
3. Camunda service consumes the event
4. Process instance is created in `ACT_RU_PROCESS_INSTANCE`
5. Variables are stored in `ACT_RU_VARIABLE`
6. First user task appears in `ACT_RU_TASK`

### Test 2: Verify Process Instance Creation

```sql
SELECT * FROM ACT_RU_PROCESS_INSTANCE;
SELECT * FROM ACT_RU_VARIABLE;
SELECT * FROM ACT_RU_TASK;
```

### Test 3: Complete Tasks via Frontend

1. Open React app at `http://localhost:3000`
2. Filter by candidate group: `order-managers`
3. Enter user ID and claim the "Review Order" task
4. Complete the task with approval
5. Check for next task (Payment Approval)

### Test 4: Complete Tasks via REST API

**Claim Task**:
```bash
curl -X POST "http://localhost:5050/api/camunda/tasks/{taskId}/claim?userId=manager1"
```

**Complete Task**:
```bash
curl -X POST "http://localhost:5050/api/camunda/tasks/{taskId}/complete" \
  -H "Content-Type: application/json" \
  -d '{"reviewApproved": true, "reviewComments": "Order looks good"}'
```

### Test 5: Monitor Process Flow

Use the React frontend to:
1. View all active tasks
2. Complete tasks in sequence
3. Monitor process variables
4. View process instances

---

## Camunda Modeler Integration

### Step 1: Download Camunda Modeler

Download from: https://camunda.com/download/modeler/

### Step 2: Open the BPMN File

1. Open Camunda Modeler
2. File → Open
3. Navigate to: `camunda-service/src/main/resources/processes/order-process.bpmn`

### Step 3: Understanding the Model

**Elements Used**:
- **Start Event**: Green circle - Process entry point
- **Service Task**: Rounded rectangle with gear icon - Automatic tasks
- **User Task**: Rounded rectangle with user icon - Manual tasks requiring human interaction
- **Exclusive Gateway**: Diamond shape - Decision points
- **End Event**: Red circle - Process exit points
- **Sequence Flow**: Arrows connecting elements

### Step 4: Modifying the Process

#### Adding a New User Task:

1. Drag a **User Task** from the palette
2. Connect it with sequence flows
3. Configure the task:
   - **ID**: Unique identifier (e.g., `UserTask_NewTask`)
   - **Name**: Display name
   - **Candidate Groups**: Comma-separated groups (e.g., `sales-team`)
   - **Form Key**: Optional form identifier

#### Adding a Service Task:

1. Drag a **Service Task** from the palette
2. Configure:
   - **ID**: Unique identifier
   - **Name**: Display name
   - **Implementation**: Select "Delegate Expression"
   - **Delegate Expression**: Enter bean name (e.g., `${myDelegate}`)

3. Create corresponding Java delegate:
```java
@Component("myDelegate")
public class MyDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        // Your logic here
    }
}
```

#### Adding a Gateway:

1. Drag an **Exclusive Gateway** from the palette
2. Connect sequence flows
3. Add conditions to flows:
   - Right-click flow → Properties
   - Add condition: `${variable == value}`

### Step 5: Deploying Changes

#### Option 1: Automatic Deployment (Development)

- Place BPMN file in `src/main/resources/processes/`
- Restart Camunda service
- Process is automatically deployed

#### Option 2: Manual Deployment via REST API

```bash
curl -X POST "http://localhost:5050/engine-rest/deployment/create" \
  -F "deployment-name=order-process" \
  -F "deployment-source=modeler" \
  -F "order-process.bpmn=@order-process.bpmn"
```

#### Option 3: Via Camunda Cockpit

1. Access Camunda Cockpit: `http://localhost:5050/camunda/app/cockpit/default/`
2. Login: admin/admin
3. Navigate to Deployments
4. Upload BPMN file

### Step 6: Testing in Modeler

1. **Validate**: Click Validate button (checkmark icon)
2. **Check for Errors**: Red markers indicate issues
3. **Preview**: Use "Preview" to see XML

### Step 7: Best Practices

1. **Naming Conventions**:
   - Task IDs: `UserTask_DescriptiveName` or `ServiceTask_DescriptiveName`
   - Process ID: `process_name` (lowercase with underscores)

2. **Variable Management**:
   - Use consistent variable names
   - Document variable types and purposes
   - Initialize variables at process start

3. **Error Handling**:
   - Add error boundary events for service tasks
   - Use try-catch in delegates
   - Implement retry mechanisms

4. **Performance**:
   - Use async service tasks for long-running operations
   - Avoid blocking operations in delegates
   - Use timers for delays instead of sleep

### Step 8: Process Versioning

When you modify and redeploy a process:
- Camunda creates a new version
- Old process instances continue with old version
- New instances use new version
- Version number increments automatically

---

## Troubleshooting

### Issue 1: Tables Not Created

**Symptoms**: No ACT_* tables in database

**Solutions**:
1. Check database connection in `camunda-service.properties`
2. Verify `camunda.bpm.database.schema-update=true`
3. Check application logs for database errors
4. Ensure MySQL user has CREATE TABLE permissions

### Issue 2: Process Not Starting

**Symptoms**: Order created but no process instance

**Solutions**:
1. Check Kafka connection
2. Verify event is being published
3. Check Camunda service logs
4. Verify process definition is deployed:
   ```bash
   curl http://localhost:5050/engine-rest/process-definition
   ```

### Issue 3: Tasks Not Appearing

**Symptoms**: Process instance exists but no tasks

**Solutions**:
1. Check process flow - ensure it reaches user task
2. Verify task candidate groups
3. Check ACT_RU_TASK table directly
4. Review process execution in Camunda Cockpit

### Issue 4: Variables Not Updating

**Symptoms**: Variables not changing when completing tasks

**Solutions**:
1. Verify variable names match process definition
2. Check variable types (Boolean vs String)
3. Ensure variables are passed in complete request
4. Check ACT_RU_VARIABLE table

### Issue 5: React App Can't Connect

**Symptoms**: Frontend shows connection errors

**Solutions**:
1. Verify Camunda service is running on port 5050
2. Check CORS configuration (if needed)
3. Verify API URL in `.env` file
4. Check browser console for errors

### Issue 6: Process Stuck

**Symptoms**: Process instance exists but not progressing

**Solutions**:
1. Check for failed jobs in ACT_RU_JOB
2. Review service task delegate errors
3. Check gateway conditions
4. Use Camunda Cockpit to inspect execution

---

## Additional Resources

- **Camunda Documentation**: https://docs.camunda.org/
- **Camunda REST API**: https://docs.camunda.org/manual/7.21/reference/rest/
- **BPMN 2.0 Specification**: https://www.omg.org/spec/BPMN/2.0/
- **Camunda Modeler Guide**: https://docs.camunda.org/manual/7.21/modeler/

---

## Summary

This integration provides:

✅ **Complete workflow automation** for order processing
✅ **User task management** via React frontend
✅ **REST API** for programmatic task management
✅ **Database persistence** with proper Camunda tables
✅ **Event-driven architecture** via Kafka
✅ **Comprehensive documentation** for setup and usage

The system is now ready for production use with proper error handling, logging, and monitoring capabilities.

