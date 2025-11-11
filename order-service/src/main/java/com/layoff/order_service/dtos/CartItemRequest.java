package com.layoff.order_service.dtos;

import lombok.Data;

@Data
public class CartItemRequest {
    private String productId;
    private Integer quantity;
}








