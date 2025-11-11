package com.layoff.order_service.services;

import com.layoff.order_service.dtos.OrderItemDTO;
import com.layoff.order_service.dtos.OrderCreatedEvent;
import com.layoff.order_service.dtos.OrderResponse;
import com.layoff.order_service.models.CartItem;
import com.layoff.order_service.models.Order;
import com.layoff.order_service.models.OrderItem;
import com.layoff.order_service.models.OrderStatus;
import com.layoff.order_service.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final StreamBridge streamBridge;
    
    @Transactional
    public Optional<OrderResponse> createOrder(String userId) {
        logger.info("=== ORDER CREATION FLOW STARTED ===");
        logger.info("Creating order for userId: {}", userId);

        List<CartItem> cartItems = cartService.getCart(userId);
        logger.info("Retrieved {} cart items for userId: {}", cartItems.size(), userId);

        if (cartItems.isEmpty()) {
            logger.warn("Cart is empty for userId: {}. Order creation aborted.", userId);
            return Optional.empty();
        }

        BigDecimal totalPrice = cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        logger.info("Calculated total price: {} for userId: {}", totalPrice, userId);

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(totalPrice);

        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setOrder(order);
            return orderItem;
        }).toList();
        order.setItems(orderItems);
        logger.info("Created order object with {} items for userId: {}", orderItems.size(), userId);

        logger.info("Saving order to database for userId: {}", userId);
        Order savedOrder = orderRepository.save(order);
        logger.info("Order saved successfully. OrderId: {}, UserId: {}, TotalAmount: {}, Status: {}", 
                savedOrder.getId(), savedOrder.getUserId(), savedOrder.getTotalAmount(), savedOrder.getStatus());
        
        logger.info("Clearing cart for userId: {}", userId);
        cartService.clearCart(userId);
        logger.info("Cart cleared for userId: {}", userId);

        // Produce OrderCreated event
        logger.info("Creating OrderCreatedEvent for OrderId: {}", savedOrder.getId());
        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getStatus(),
                mapOrderItemsToDTOs(savedOrder.getItems()),
                savedOrder.getTotalAmount(),
                savedOrder.getCreatedAt()
        );

        logger.info("Publishing OrderCreatedEvent to Kafka topic 'orders.created' via binding 'orderCreated-out-0'");
        boolean sent = streamBridge.send("orderCreated-out-0", event);
        if (sent) {
            logger.info("OrderCreatedEvent published successfully to Kafka. OrderId: {}", savedOrder.getId());
        } else {
            logger.error("FAILED to publish OrderCreatedEvent to Kafka. OrderId: {}. Event may not be processed.", savedOrder.getId());
        }
        logger.info("=== ORDER CREATION FLOW COMPLETED ===");

        return Optional.of(mapToOrderResponse(savedOrder));
    }

    private OrderResponse mapToOrderResponse(Order savedOrder) {
        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                savedOrder.getItems().stream().map(item -> new OrderItemDTO(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice().multiply(new BigDecimal(item.getQuantity()))
                )).toList(),
                savedOrder.getCreatedAt()
        );
    }

    private List<OrderItemDTO> mapOrderItemsToDTOs(List<OrderItem> items) {
        return items.stream().map(item -> new OrderItemDTO(
                item.getId(),
                item.getProductId(),
                item.getQuantity(),
                item.getPrice(),
                item.getPrice().multiply(new BigDecimal(item.getQuantity()))
        )).collect(Collectors.toList());
    }
}
