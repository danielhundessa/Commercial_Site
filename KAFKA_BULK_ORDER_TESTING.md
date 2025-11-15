# Kafka Bulk Order Testing Guide - JMeter & Java

Step-by-step guide to send hundreds or thousands of orders to test Kafka using JMeter and Java.

## Prerequisites

- Kafka running on `localhost:9092`
- Topic `orders.created` exists
- Order Service running on `localhost:6060` (for JMeter method)
- JMeter installed
- Java 17+ and Maven (for Java method)

---

## Method 1: Java Kafka Producer (Direct to Kafka)

### Step 1: Create Test Class

Create file: `order-service/src/test/java/com/layoff/order_service/BulkOrderProducerTest.java`

```java
package com.layoff.order_service;

import com.layoff.order_service.dtos.OrderCreatedEvent;
import com.layoff.order_service.dtos.OrderItemDTO;
import com.layoff.order_service.models.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class BulkOrderProducerTest {
    
    @Autowired
    private StreamBridge streamBridge;
    
    private final Random random = new Random();
    
    @Test
    public void sendBulkOrders() {
        int numOrders = 1000;
        int batchSize = 100;
        
        long startTime = System.currentTimeMillis();
        int sentCount = 0;
        
        System.out.println("Starting bulk order generation...");
        
        for (int i = 1; i <= numOrders; i++) {
            String userId = "user-" + (random.nextInt(100) + 1);
            OrderCreatedEvent event = generateOrderEvent((long) i, userId);
            
            boolean sent = streamBridge.send("orderCreated-out-0", event);
            if (sent) {
                sentCount++;
            } else {
                System.err.println("Failed to send order: " + i);
            }
            
            if (i % batchSize == 0) {
                long elapsed = System.currentTimeMillis() - startTime;
                double rate = (double) sentCount / (elapsed / 1000.0);
                System.out.printf("Sent %d/%d orders (%.2f orders/sec)%n", 
                    sentCount, numOrders, rate);
            }
            
            // Small delay to avoid overwhelming Kafka
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        double rate = (double) sentCount / (totalTime / 1000.0);
        System.out.printf("\n✅ Completed: %d orders in %.2fs (%.2f orders/sec)%n", 
            sentCount, totalTime / 1000.0, rate);
    }
    
    private OrderCreatedEvent generateOrderEvent(Long orderId, String userId) {
        int numItems = random.nextInt(5) + 1;
        List<OrderItemDTO> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (int i = 0; i < numItems; i++) {
            int quantity = random.nextInt(10) + 1;
            BigDecimal price = BigDecimal.valueOf(random.nextDouble() * 90 + 10)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(subtotal);
            
            items.add(new OrderItemDTO(
                (long) (i + 1),
                "prod-" + (random.nextInt(900) + 100),
                quantity,
                price,
                subtotal
            ));
        }
        
        return new OrderCreatedEvent(
            orderId,
            userId,
            OrderStatus.CONFIRMED,
            items,
            totalAmount,
            LocalDateTime.now()
        );
    }
}
```

### Step 2: Run the Test

```bash
cd order-service
./mvnw test -Dtest=BulkOrderProducerTest
```

### Step 3: Verify Messages in Kafka

```bash
# Check message count
docker exec commercial_site-kafka-1 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic orders.created \
  --from-beginning \
  --timeout-ms 5000 | wc -l
```

---

## Method 2: JMeter Test Plan (REST API)

### Step 1: Install JMeter

Download from: https://jmeter.apache.org/download_jmeter.cgi

### Step 2: Create JMeter Test Plan

#### 2.1: Setup Thread Group

1. Open JMeter
2. Right-click Test Plan → Add → Threads (Users) → Thread Group
3. Configure:
   - **Number of Threads**: 10 (concurrent users)
   - **Ramp-up Period**: 10 seconds
   - **Loop Count**: 100 (100 orders per user = 1000 total)

#### 2.2: Add HTTP Request Defaults

1. Right-click Thread Group → Add → Config Element → HTTP Request Defaults
2. Configure:
   - **Server Name**: `localhost`
   - **Port Number**: `6060`
   - **Protocol**: `http`

#### 2.3: Add HTTP Header Manager

1. Right-click Thread Group → Add → Config Element → HTTP Header Manager
2. Add Header:
   - **Name**: `X-User-ID`
   - **Value**: `${__RandomString(10,user-)}${__threadNum}`

#### 2.4: Add Items to Cart (First Request)

1. Right-click Thread Group → Add → Sampler → HTTP Request
2. Name: `Add Item to Cart`
3. Configure:
   - **Method**: `POST`
   - **Path**: `/api/cart/items`
   - **Body Data** (tab):
     ```json
     {
       "productId": "${__Random(1,10)}",
       "quantity": ${__Random(1,5)}
     }
     ```
4. Add Header Manager to this request:
   - Right-click "Add Item to Cart" → Add → Config Element → HTTP Header Manager
   - Add: `Content-Type: application/json`

#### 2.5: Create Order (Second Request)

1. Right-click Thread Group → Add → Sampler → HTTP Request
2. Name: `Create Order`
3. Configure:
   - **Method**: `POST`
   - **Path**: `/api/orders`
   - **Body Data**: (leave empty)

#### 2.6: Add View Results Tree (Optional - for debugging)

1. Right-click Thread Group → Add → Listener → View Results Tree
2. Use only for debugging (disable for performance tests)

#### 2.7: Add Summary Report

1. Right-click Thread Group → Add → Listener → Summary Report
2. This shows performance metrics

### Step 3: Save Test Plan

1. File → Save Test Plan As
2. Save as: `bulk_order_test.jmx`

### Step 4: Run Test Plan

#### Option A: GUI Mode (for testing)
1. Click green "Play" button
2. Watch results in Summary Report

#### Option B: Command Line (for performance)
```bash
jmeter -n -t bulk_order_test.jmx -l results.jtl -e -o report/
```

### Step 5: View Results

- **GUI**: Check Summary Report listener
- **Command Line**: Open `report/index.html` in browser

---

## Method 3: JMeter with CSV Data (Advanced)

### Step 1: Create CSV File

Create `user_ids.csv`:
```csv
userId
user-1
user-2
user-3
...
user-1000
```

### Step 2: Add CSV Data Set Config

1. Right-click Thread Group → Add → Config Element → CSV Data Set Config
2. Configure:
   - **Filename**: `user_ids.csv`
   - **Variable Names**: `userId`
   - **Delimiter**: `,`
   - **Recycle on EOF**: `true`
   - **Stop thread on EOF**: `false`

### Step 3: Update HTTP Header Manager

Change `X-User-ID` value to: `${userId}`

### Step 4: Run Test

Same as Method 2, Step 4.

---

## Performance Testing Scenarios

### Scenario 1: Burst Load (1000 orders quickly)

**Java Method:**
```java
// In BulkOrderProducerTest.java, modify:
int numOrders = 1000;
Thread.sleep(0); // No delay
```

**JMeter Method:**
- Threads: 50
- Ramp-up: 1 second
- Loop: 20

### Scenario 2: Sustained Load (100 orders/sec)

**Java Method:**
```java
int numOrders = 1000;
Thread.sleep(10); // 10ms = ~100/sec
```

**JMeter Method:**
- Threads: 10
- Ramp-up: 10 seconds
- Loop: 100
- Add Constant Throughput Timer: 6000 (100/min = ~1.67/sec per thread)

### Scenario 3: Gradual Ramp-up

**JMeter Method:**
1. Create multiple Thread Groups
2. Group 1: 10 threads, ramp 10s, loop 10
3. Group 2: 20 threads, ramp 10s, loop 10
4. Group 3: 50 threads, ramp 10s, loop 10
5. Run sequentially

---

## Monitoring & Verification

### Check Kafka Messages

```bash
# Count messages
docker exec commercial_site-kafka-1 kafka-run-class kafka.tools.GetOffsetShell \
  --broker-list localhost:9092 \
  --topic orders.created

# View recent messages
docker exec commercial_site-kafka-1 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic orders.created \
  --from-beginning \
  --max-messages 10
```

### Check Consumer Lag

```bash
docker exec commercial_site-kafka-1 kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe --group camunda-service
```

### Check Camunda Process Instances

```sql
USE camundadb;

-- Count process instances
SELECT COUNT(*) FROM ACT_RU_PROCESS_INSTANCE 
WHERE PROC_DEF_KEY_ = 'order_process';

-- Recent instances
SELECT * FROM ACT_RU_PROCESS_INSTANCE 
WHERE PROC_DEF_KEY_ = 'order_process' 
ORDER BY START_TIME_ DESC 
LIMIT 10;
```

---

## Troubleshooting

### Java Test Issues

**Problem**: Test fails to connect to Kafka
- **Solution**: Ensure Kafka is running: `docker ps | grep kafka`

**Problem**: Messages not appearing
- **Solution**: Check topic exists: `docker exec commercial_site-kafka-1 kafka-topics --bootstrap-server localhost:9092 --list`

### JMeter Issues

**Problem**: 400 Bad Request
- **Solution**: Ensure cart has items before creating order. Add multiple "Add Item to Cart" requests.

**Problem**: Connection refused
- **Solution**: Verify Order Service is running on port 6060

**Problem**: Slow performance
- **Solution**: Disable View Results Tree listener during performance tests

---

## Recommended Settings

### For Quick Testing (100 orders)
- **Java**: `numOrders = 100`, `Thread.sleep(10)`
- **JMeter**: 5 threads, 5 ramp-up, 20 loops

### For Load Testing (1000 orders)
- **Java**: `numOrders = 1000`, `Thread.sleep(10)`
- **JMeter**: 10 threads, 10 ramp-up, 100 loops

### For Stress Testing (10000 orders)
- **Java**: `numOrders = 10000`, `Thread.sleep(0)`
- **JMeter**: 50 threads, 20 ramp-up, 200 loops

---

**Last Updated**: 2024-01-15

