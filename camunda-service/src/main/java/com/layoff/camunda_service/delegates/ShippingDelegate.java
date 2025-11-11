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
        Long orderId = (Long) execution.getVariable("orderId");
        
        logger.info("Shipping order: {}", orderId);
        
        // Simulate shipping process
        String trackingNumber = "TRACK-" + System.currentTimeMillis();
        execution.setVariable("trackingNumber", trackingNumber);
        execution.setVariable("orderShipped", true);
        
        logger.info("Order {} shipped successfully. Tracking Number: {}", orderId, trackingNumber);
    }
}



