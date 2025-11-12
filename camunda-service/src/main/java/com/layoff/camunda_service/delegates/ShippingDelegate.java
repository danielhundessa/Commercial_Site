package com.layoff.camunda_service.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("shippingDelegate")
public class ShippingDelegate implements JavaDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(ShippingDelegate.class);
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = getOrderIdAsLong(execution);
        
        logger.info("Shipping order: {}", orderId);
        
        // Simulate shipping process
        String trackingNumber = "TRACK-" + System.currentTimeMillis();
        execution.setVariable("trackingNumber", trackingNumber);
        execution.setVariable("orderShipped", true);
        
        logger.info("Order {} shipped successfully. Tracking Number: {}", orderId, trackingNumber);
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
}




