package com.layoff.camunda_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.camunda.bpm.engine.RuntimeService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

record OrderItemDTO(Long id, String productId, Integer quantity) {}
record OrderCreatedEvent(Long orderId, String userId, String status, java.util.List<OrderItemDTO> items, java.math.BigDecimal totalAmount, java.time.LocalDateTime createdAt) {}

@SpringBootApplication
public class CamundaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CamundaServiceApplication.class, args);
	}

	@Bean
	public Consumer<OrderCreatedEvent> orderCreated(RuntimeService runtimeService) {
		return event -> {
			Map<String, Object> vars = new HashMap<>();
			vars.put("orderId", event.orderId());
			vars.put("userId", event.userId());
			vars.put("totalAmount", event.totalAmount());
			runtimeService.startProcessInstanceByKey("order_process", vars);
		};
	}
}
