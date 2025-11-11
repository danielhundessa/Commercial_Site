package com.layoff.camunda_service.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("paymentProcessingDelegate")
public class PaymentProcessingDelegate implements JavaDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessingDelegate.class);
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = (Long) execution.getVariable("orderId");
        BigDecimal totalAmount = (BigDecimal) execution.getVariable("totalAmount");
        
        logger.info("Processing payment for order: {} with amount: {}", orderId, totalAmount);
        
        // Simulate payment processing
        // In a real scenario, this would integrate with a payment gateway
        String paymentId = "PAY-" + System.currentTimeMillis();
        execution.setVariable("paymentId", paymentId);
        execution.setVariable("paymentProcessed", true);
        
        logger.info("Payment processed successfully. Payment ID: {}", paymentId);
    }
}



