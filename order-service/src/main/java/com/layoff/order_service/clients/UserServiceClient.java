package com.layoff.order_service.clients;

import com.layoff.user_service.dtos.UserResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface UserServiceClient {

    @GetExchange("/api/users/{id}")
    UserResponse getUserDetails(@PathVariable String id);
}