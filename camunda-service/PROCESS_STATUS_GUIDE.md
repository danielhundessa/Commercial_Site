# Order Process Status Guide

This guide explains how to view the order process status, see which steps are completed, and which step is currently active.

## Process Flow Overview

The Order Processing Workflow consists of the following steps:

1. **Order Received** (Start Event) - Order is created
2. **Validate Order** (Service Task) - Automatic validation
3. **Review Order** (User Task) - Assigned to `order_managers` group
4. **Review Decision** (Gateway) - Approved or Rejected
5. **Approve Payment** (User Task) - Assigned to `finance_team` group
6. **Payment Decision** (Gateway) - Payment Approved or Rejected
7. **Process Payment** (Service Task) - Automatic payment processing
8. **Prepare Shipping** (User Task) - Assigned to `warehouse_team` group
9. **Ship Order** (Service Task) - Automatic shipping
10. **Confirm Delivery** (User Task) - Assigned to `delivery_team` group
11. **Order Completed** (End Event) - Process completed

## Sample Users

The following users are automatically created when the Camunda service starts:

### Order Managers Group (`order_managers`)
- **manager1** / **manager1** - John Manager
- **manager2** / **manager2** - Sarah Manager

### Finance Team Group (`finance_team`)
- **finance1** / **finance1** - Michael Finance
- **finance2** / **finance2** - Emily Finance

### Warehouse Team Group (`warehouse_team`)
- **warehouse1** / **warehouse1** - Mike Warehouse
- **warehouse2** / **warehouse2** - Lisa Warehouse

### Delivery Team Group (`delivery_team`)
- **delivery1** / **delivery1** - David Delivery
- **delivery2** / **delivery2** - Anna Delivery

**Note:** All passwords are the same as the username (e.g., `manager1`/`manager1`)

## Viewing Process Status

### 1. Via REST API

#### Get Process Instance with Status
```bash
GET http://localhost:5050/api/camunda/process-instances/{processInstanceId}
```

Response includes:
- Process instance details
- Variables
- **Status object** with:
  - `completedActivities`: List of completed steps with timestamps
  - `activeActivities`: List of currently active steps
  - `isEnded`: Whether the process has completed

#### Get Process Status Only
```bash
GET http://localhost:5050/api/camunda/process-instances/{processInstanceId}/status
```

Example Response:
```json
{
  "completedActivities": [
    {
      "activityId": "StartEvent_OrderReceived",
      "activityName": "Order Received",
      "activityType": "startEvent",
      "startTime": "2025-11-11T18:00:00",
      "endTime": "2025-11-11T18:00:01",
      "duration": 1000
    },
    {
      "activityId": "ServiceTask_ValidateOrder",
      "activityName": "Validate Order",
      "activityType": "serviceTask",
      "startTime": "2025-11-11T18:00:01",
      "endTime": "2025-11-11T18:00:02",
      "duration": 1000
    }
  ],
  "activeActivities": [
    {
      "activityId": "UserTask_ReviewOrder",
      "activityName": "Review Order",
      "activityType": "userTask"
    }
  ],
  "isEnded": false
}
```

### 2. Via Camunda Cockpit (Web UI)

1. Access Camunda Cockpit at: `http://localhost:5050/camunda/app/cockpit/default/`
2. Login with: `admin` / `admin`
3. Navigate to **Process Instances**
4. Click on a process instance to see:
   - **Completed activities** (shown in green/gray)
   - **Active activities** (shown in blue/highlighted)
   - **Process variables**
   - **Task details**

### 3. Via React Frontend

The React frontend at `http://localhost:3000` shows:
- **Process Instances** page: List of all process instances
- **Tasks** page: Active tasks that can be claimed and completed
- Click on a process instance to see details

## Assigning Tasks to Users

### Via REST API

1. **Claim a task** (assign to user):
```bash
POST http://localhost:5050/api/camunda/tasks/{taskId}/claim?userId=manager1
```

2. **Complete a task**:
```bash
POST http://localhost:5050/api/camunda/tasks/{taskId}/complete
Content-Type: application/json

{
  "reviewApproved": true,
  "reviewComments": "Order looks good"
}
```

### Via React Frontend

1. Navigate to **Tasks** page
2. Filter by candidate group (e.g., `order_managers`)
3. Click **Claim** button and enter user ID (e.g., `manager1`)
4. Click on task to view details
5. Fill in form and click **Complete Task**

## Process Status Indicators

- **Completed Activities**: Steps that have finished execution
  - Service tasks complete automatically
  - User tasks complete when assigned user completes them
  
- **Active Activities**: Steps currently being executed
  - Service tasks show as active while running
  - User tasks show as active when waiting for user action

- **Process Ended**: When `isEnded: true`, the process has completed or been terminated

## Troubleshooting

### Users Not Created
- Check application logs for initialization messages
- Verify Camunda service started successfully
- Users are created on first startup only (idempotent)

### Cannot See Process Status
- Ensure process instance exists: `GET /api/camunda/process-instances`
- Check that history level is set to `full` in `camunda-service.properties`
- Verify process instance ID is correct

### Tasks Not Showing
- Check candidate group matches user's group membership
- Verify task is not already assigned to another user
- Use `GET /api/camunda/tasks?candidateGroup=order_managers` to filter

