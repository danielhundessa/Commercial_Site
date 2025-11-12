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
        Long orderId = getOrderIdAsLong(execution);
        BigDecimal totalAmount = getTotalAmountAsBigDecimal(execution);
        
        logger.info("Processing payment for order: {} with amount: {}", orderId, totalAmount);
        
        // Simulate payment processing
        // In a real scenario, this would integrate with a payment gateway
        String paymentId = "PAY-" + System.currentTimeMillis();
        execution.setVariable("paymentId", paymentId);
        execution.setVariable("paymentProcessed", true);
        
        logger.info("Payment processed successfully. Payment ID: {}", paymentId);
    }
    
    /**
     * Safely extracts orderId as Long from process variables.
     * Handles both Integer and Long types that Camunda might store.
     */
    private Long getOrderIdAsLong(DelegateExecution execution) {
        Object orderIdObj = execution.getVariable("orderId");
        if (orderIdObj == null) {
            throw new IllegalStateException("orderId variable is null");
        }
        if (orderIdObj instanceof Long) {
            return (Long) orderIdObj;
        } else if (orderIdObj instanceof Integer) {
            return ((Integer) orderIdObj).longValue();
        } else if (orderIdObj instanceof Number) {
            return ((Number) orderIdObj).longValue();
        } else {
            throw new ClassCastException("orderId must be a number, but was: " + orderIdObj.getClass().getName());
        }
    }
    
    /**
     * Safely extracts totalAmount as BigDecimal from process variables.
     * Handles Double, Float, BigDecimal, String, Integer, and Long types that Camunda might store.
     */
    private BigDecimal getTotalAmountAsBigDecimal(DelegateExecution execution) {
        Object totalAmountObj = execution.getVariable("totalAmount");
        if (totalAmountObj == null) {
            throw new IllegalStateException("totalAmount variable is null");
        }
        if (totalAmountObj instanceof BigDecimal) {
            return (BigDecimal) totalAmountObj;
        } else if (totalAmountObj instanceof Double) {
            return BigDecimal.valueOf((Double) totalAmountObj);
        } else if (totalAmountObj instanceof Float) {
            return BigDecimal.valueOf((Float) totalAmountObj);
        } else if (totalAmountObj instanceof Long) {
            return BigDecimal.valueOf((Long) totalAmountObj);
        } else if (totalAmountObj instanceof Integer) {
            return BigDecimal.valueOf((Integer) totalAmountObj);
        } else if (totalAmountObj instanceof String) {
            return new BigDecimal((String) totalAmountObj);
        } else if (totalAmountObj instanceof Number) {
            return BigDecimal.valueOf(((Number) totalAmountObj).doubleValue());
        } else {
            throw new ClassCastException("totalAmount must be a number or string, but was: " + totalAmountObj.getClass().getName());
        }
    }
}




