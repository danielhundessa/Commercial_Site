package com.layoff.notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.function.Consumer;

record OrderItemDTO(Long id, String productId, Integer quantity) {}
record OrderCreatedEvent(Long orderId, String userId, String status, java.util.List<OrderItemDTO> items, java.math.BigDecimal totalAmount, java.time.LocalDateTime createdAt) {}

@SpringBootApplication
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

	@Bean
	public Consumer<OrderCreatedEvent> orderCreated(JavaMailSender mailSender) {
		return event -> {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo("danielshundessa@gmail.com");
			message.setSubject("Order Created: #" + event.orderId());
			message.setText("Order " + event.orderId() + " was created for user " + event.userId() +
					" with total " + event.totalAmount() + ". Items: " + event.items().size());
			mailSender.send(message);
		};
	}
}
