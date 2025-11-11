package com.layoff.camunda_service.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("orderValidationDelegate")
public class OrderValidationDelegate implements JavaDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderValidationDelegate.class);
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = (Long) execution.getVariable("orderId");
        String userId = (String) execution.getVariable("userId");
        
        logger.info("Validating order: {} for user: {}", orderId, userId);
        
        // Perform order validation logic here
        // For now, we'll just log and set validation result
        boolean isValid = true;
        
        execution.setVariable("orderValidated", isValid);
        logger.info("Order {} validation completed. Valid: {}", orderId, isValid);
    }
}

