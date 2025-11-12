package com.layoff.camunda_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Initializes sample users and groups in Camunda for task assignment.
 * This inserts data directly into Camunda identity tables (ACT_ID_USER, ACT_ID_GROUP, ACT_ID_MEMBERSHIP).
 * This runs on application startup to create users for each candidate group.
 */
@Component
public class CamundaDataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(CamundaDataInitializer.class);
    
    private final JdbcTemplate jdbcTemplate;
    
    public CamundaDataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("=== INITIALIZING CAMUNDA USERS AND GROUPS (Direct Database Insert) ===");
        
        try {
            // Create Groups (using underscores - Camunda doesn't allow hyphens in resource IDs)
            createGroupIfNotExists("order_managers", "Order Managers", "GROUP");
            createGroupIfNotExists("finance_team", "Finance Team", "GROUP");
            createGroupIfNotExists("warehouse_team", "Warehouse Team", "GROUP");
            createGroupIfNotExists("delivery_team", "Delivery Team", "GROUP");
            
            // Create Users for Order Managers
            createUserIfNotExists("manager1", "John", "Manager", "john.manager@example.com", "manager1");
            createUserIfNotExists("manager2", "Sarah", "Manager", "sarah.manager@example.com", "manager2");
            assignUserToGroup("manager1", "order_managers");
            assignUserToGroup("manager2", "order_managers");
            
            // Create Users for Finance Team
            createUserIfNotExists("finance1", "Michael", "Finance", "finance1@example.com", "finance1");
            createUserIfNotExists("finance2", "Emily", "Finance", "emily.finance@example.com", "finance2");
            assignUserToGroup("finance1", "finance_team");
            assignUserToGroup("finance2", "finance_team");
            
            // Create Users for Warehouse Team
            createUserIfNotExists("warehouse1", "Mike", "Warehouse", "mike.warehouse@example.com", "warehouse1");
            createUserIfNotExists("warehouse2", "Lisa", "Warehouse", "lisa.warehouse@example.com", "warehouse2");
            assignUserToGroup("warehouse1", "warehouse_team");
            assignUserToGroup("warehouse2", "warehouse_team");
            
            // Create Users for Delivery Team
            createUserIfNotExists("delivery1", "David", "Delivery", "david.delivery@example.com", "delivery1");
            createUserIfNotExists("delivery2", "Anna", "Delivery", "anna.delivery@example.com", "delivery2");
            assignUserToGroup("delivery1", "delivery_team");
            assignUserToGroup("delivery2", "delivery_team");
            
            logger.info("=== CAMUNDA USERS AND GROUPS INITIALIZATION COMPLETED ===");
            logger.info("Sample users created:");
            logger.info("  Order Managers: manager1, manager2");
            logger.info("  Finance Team: finance1, finance2");
            logger.info("  Warehouse Team: warehouse1, warehouse2");
            logger.info("  Delivery Team: delivery1, delivery2");
            logger.info("All users have password same as username (e.g., manager1/manager1)");
        } catch (Exception e) {
            logger.error("Error initializing Camunda users and groups", e);
            // Don't fail startup if initialization fails
        }
    }
    
    /**
     * Creates a group in ACT_ID_GROUP table if it doesn't exist
     */
    private void createGroupIfNotExists(String groupId, String groupName, String groupType) {
        try {
            // Check if group exists
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ACT_ID_GROUP WHERE ID_ = ?",
                Integer.class,
                groupId
            );
            
            if (count == null || count == 0) {
                // Insert group - REV_ is revision number (starts at 1), TYPE_ is group type
                jdbcTemplate.update(
                    "INSERT INTO ACT_ID_GROUP (ID_, REV_, NAME_, TYPE_) VALUES (?, ?, ?, ?)",
                    groupId, 1, groupName, groupType
                );
                logger.info("Created group: {} ({})", groupId, groupName);
            } else {
                logger.debug("Group already exists: {}", groupId);
            }
        } catch (Exception e) {
            logger.error("Error creating group {}: {}", groupId, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Creates a user in ACT_ID_USER table if it doesn't exist
     * Passwords are hashed using MD5 (Camunda's default)
     */
    private void createUserIfNotExists(String userId, String firstName, String lastName, String email, String password) {
        try {
            // Check if user exists
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ACT_ID_USER WHERE ID_ = ?",
                Integer.class,
                userId
            );
            
            if (count == null || count == 0) {
                // Hash password using MD5 (Camunda's default)
                String hashedPassword = hashPassword(password);
                
                // Insert user - REV_ is revision number (starts at 1)
                jdbcTemplate.update(
                    "INSERT INTO ACT_ID_USER (ID_, REV_, FIRST_, LAST_, EMAIL_, PWD_) VALUES (?, ?, ?, ?, ?, ?)",
                    userId, 1, firstName, lastName, email, hashedPassword
                );
                logger.info("Created user: {} ({} {})", userId, firstName, lastName);
            } else {
                logger.debug("User already exists: {}", userId);
            }
        } catch (Exception e) {
            logger.error("Error creating user {}: {}", userId, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Assigns a user to a group in ACT_ID_MEMBERSHIP table
     */
    private void assignUserToGroup(String userId, String groupId) {
        try {
            // Check if membership exists
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ACT_ID_MEMBERSHIP WHERE USER_ID_ = ? AND GROUP_ID_ = ?",
                Integer.class,
                userId, groupId
            );
            
            if (count == null || count == 0) {
                // Insert membership
                jdbcTemplate.update(
                    "INSERT INTO ACT_ID_MEMBERSHIP (USER_ID_, GROUP_ID_) VALUES (?, ?)",
                    userId, groupId
                );
                logger.info("Assigned user {} to group {}", userId, groupId);
            } else {
                logger.debug("User {} already member of group {}", userId, groupId);
            }
        } catch (Exception e) {
            logger.error("Error assigning user {} to group {}: {}", userId, groupId, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Hashes password using MD5 (Camunda's default password hashing)
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("MD5 algorithm not available", e);
            throw new RuntimeException("Password hashing failed", e);
        }
    }
}

