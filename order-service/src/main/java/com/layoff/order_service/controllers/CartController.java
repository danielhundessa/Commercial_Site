package com.layoff.order_service.controllers;

import com.layoff.order_service.dtos.CartItemDTO;
import com.layoff.order_service.dtos.CartItemRequest;
import com.layoff.order_service.dtos.CartResponse;
import com.layoff.order_service.models.CartItem;
import com.layoff.order_service.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader("X-User-ID") String userId
    ) {
        List<CartItem> cartItems = cartService.getCart(userId);
        
        List<CartItemDTO> cartItemDTOs = cartItems.stream()
                .map(item -> new CartItemDTO(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
        
        BigDecimal totalAmount = cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        CartResponse response = new CartResponse(userId, cartItemDTOs, totalAmount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemDTO> addToCart(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody CartItemRequest request
    ) {
        return cartService.addToCart(userId, request)
                .map(item -> {
                    CartItemDTO dto = new CartItemDTO(
                            item.getId(),
                            item.getProductId(),
                            item.getQuantity(),
                            item.getPrice(),
                            item.getPrice()
                    );
                    return new ResponseEntity<>(dto, HttpStatus.CREATED);
                })
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable Long cartItemId
    ) {
        boolean removed = cartService.removeFromCart(userId, cartItemId);
        return removed 
                ? ResponseEntity.noContent().build() 
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader("X-User-ID") String userId
    ) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
















