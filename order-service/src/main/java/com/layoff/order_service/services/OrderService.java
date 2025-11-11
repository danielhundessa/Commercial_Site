package com.layoff.order_service.services;

import com.layoff.order_service.dtos.OrderItemDTO;
import com.layoff.order_service.dtos.OrderResponse;
import com.layoff.order_service.models.CartItem;
import com.layoff.order_service.models.Order;
import com.layoff.order_service.models.OrderItem;
import com.layoff.order_service.models.OrderStatus;
import com.layoff.order_service.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
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
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final StreamBridge streamBridge;
    
    @Transactional
    public Optional<OrderResponse> createOrder(String userId) {

        List<CartItem> cartItems = cartService.getCart(userId);

        if (cartItems.isEmpty()) {
            return Optional.empty();
        }

        BigDecimal totalPrice = cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


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

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(userId);

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
