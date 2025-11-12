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
        logger.info("=== ORDER VALIDATION DELEGATE EXECUTION STARTED ===");
        logger.info("ProcessInstanceId: {}, ExecutionId: {}, ActivityInstanceId: {}", 
                execution.getProcessInstanceId(), 
                execution.getId(),
                execution.getActivityInstanceId());
        
        try {
            Long orderId = getOrderIdAsLong(execution);
            String userId = (String) execution.getVariable("userId");
            Object totalAmount = execution.getVariable("totalAmount");
            
            logger.info("Retrieved process variables - OrderId: {}, UserId: {}, TotalAmount: {}", 
                    orderId, userId, totalAmount);
            
            if (orderId == null) {
                logger.error("CRITICAL: orderId is NULL in process variables!");
            }
            if (userId == null) {
                logger.error("CRITICAL: userId is NULL in process variables!");
            }
            
            logger.info("Starting validation for OrderId: {}, UserId: {}", orderId, userId);
            
            // Perform order validation logic here
            // For now, we'll just log and set validation result
            boolean isValid = true;
            
            execution.setVariable("orderValidated", isValid);
            logger.info("Order {} validation completed successfully. Valid: {}", orderId, isValid);
            logger.info("Process will continue to next step (UserTask_ReviewOrder)");
            logger.info("=== ORDER VALIDATION DELEGATE EXECUTION COMPLETED ===");
        } catch (Exception e) {
            logger.error("ERROR: Exception in OrderValidationDelegate for ProcessInstanceId: {}", 
                    execution.getProcessInstanceId(), e);
            logger.error("Exception type: {}, Message: {}", e.getClass().getName(), e.getMessage());
            throw e; // Re-throw to let Camunda handle the error
        }
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


