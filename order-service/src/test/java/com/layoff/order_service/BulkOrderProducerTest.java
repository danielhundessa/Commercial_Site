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