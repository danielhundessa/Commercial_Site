package com.layoff.order_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class CartResponse {
    private String userId;
    private List<CartItemDTO> items;
    private BigDecimal totalAmount;
}















