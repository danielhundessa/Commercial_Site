package com.layoff.order_service.clients;

import com.layoff.product_service.dtos.ProductResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface ProductServiceClient {
    @GetExchange("/api/products/{id}")
    ProductResponse getProductDetails(@PathVariable String id);
}
