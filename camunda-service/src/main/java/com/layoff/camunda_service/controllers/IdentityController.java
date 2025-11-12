package com.layoff.camunda_service.controllers;

import com.layoff.camunda_service.dtos.GroupDTO;
import com.layoff.camunda_service.dtos.UserDTO;
import com.layoff.camunda_service.services.CamundaIdentityService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for retrieving users and groups from Camunda identity tables.
 */
@RestController
@RequestMapping("/api/camunda/identity")
@RequiredArgsConstructor
public class IdentityController {
    
    private static final Logger logger = LoggerFactory.getLogger(IdentityController.class);
    
    private final CamundaIdentityService identityService;
    
    /**
     * Get all users
     * GET /api/camunda/identity/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("=== GET ALL USERS REQUEST ===");
        List<UserDTO> users = identityService.getAllUsers();
        logger.info("Returning {} users", users.size());
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get a specific user by ID
     * GET /api/camunda/identity/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId) {
        logger.info("=== GET USER REQUEST: {} ===", userId);
        UserDTO user = identityService.getUserById(userId);
        
        if (user == null) {
            logger.warn("User {} not found", userId);
            return ResponseEntity.notFound().build();
        }
        
        logger.info("Returning user: {}", userId);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Get all groups
     * GET /api/camunda/identity/groups
     */
    @GetMapping("/groups")
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        logger.info("=== GET ALL GROUPS REQUEST ===");
        List<GroupDTO> groups = identityService.getAllGroups();
        logger.info("Returning {} groups", groups.size());
        return ResponseEntity.ok(groups);
    }
    
    /**
     * Get a specific group by ID
     * GET /api/camunda/identity/groups/{groupId}
     */
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<GroupDTO> getGroupById(@PathVariable String groupId) {
        logger.info("=== GET GROUP REQUEST: {} ===", groupId);
        GroupDTO group = identityService.getGroupById(groupId);
        
        if (group == null) {
            logger.warn("Group {} not found", groupId);
            return ResponseEntity.notFound().build();
        }
        
        logger.info("Returning group: {}", groupId);
        return ResponseEntity.ok(group);
    }
    
    /**
     * Get users in a specific group
     * GET /api/camunda/identity/groups/{groupId}/users
     */
    @GetMapping("/groups/{groupId}/users")
    public ResponseEntity<List<UserDTO>> getUsersByGroup(@PathVariable String groupId) {
        logger.info("=== GET USERS BY GROUP REQUEST: {} ===", groupId);
        List<UserDTO> users = identityService.getUsersByGroup(groupId);
        logger.info("Returning {} users for group {}", users.size(), groupId);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get groups for a specific user
     * GET /api/camunda/identity/users/{userId}/groups
     */
    @GetMapping("/users/{userId}/groups")
    public ResponseEntity<List<String>> getUserGroups(@PathVariable String userId) {
        logger.info("=== GET GROUPS FOR USER REQUEST: {} ===", userId);
        List<String> groups = identityService.getUserGroups(userId);
        logger.info("Returning {} groups for user {}", groups.size(), userId);
        return ResponseEntity.ok(groups);
    }
}



