package com.layoff.camunda_service.services;

import com.layoff.camunda_service.dtos.GroupDTO;
import com.layoff.camunda_service.dtos.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service to retrieve users and groups from Camunda identity tables in the database.
 */
@Service
public class CamundaIdentityService {
    
    private static final Logger logger = LoggerFactory.getLogger(CamundaIdentityService.class);
    
    private final JdbcTemplate jdbcTemplate;
    
    public CamundaIdentityService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * Get all users from ACT_ID_USER table
     */
    public List<UserDTO> getAllUsers() {
        try {
            String sql = "SELECT ID_, FIRST_, LAST_, EMAIL_ FROM ACT_ID_USER ORDER BY ID_";
            
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            List<UserDTO> users = new ArrayList<>();
            
            for (Map<String, Object> row : rows) {
                String userId = (String) row.get("ID_");
                String firstName = (String) row.get("FIRST_");
                String lastName = (String) row.get("LAST_");
                String email = (String) row.get("EMAIL_");
                
                // Get groups for this user
                List<String> groups = getUserGroups(userId);
                
                users.add(new UserDTO(userId, firstName, lastName, email, groups));
            }
            
            logger.info("Retrieved {} users from database", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error retrieving users from database", e);
            throw new RuntimeException("Failed to retrieve users", e);
        }
    }
    
    /**
     * Get a specific user by ID
     */
    public UserDTO getUserById(String userId) {
        try {
            String sql = "SELECT ID_, FIRST_, LAST_, EMAIL_ FROM ACT_ID_USER WHERE ID_ = ?";
            
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, userId);
            
            String firstName = (String) row.get("FIRST_");
            String lastName = (String) row.get("LAST_");
            String email = (String) row.get("EMAIL_");
            
            // Get groups for this user
            List<String> groups = getUserGroups(userId);
            
            return new UserDTO(userId, firstName, lastName, email, groups);
        } catch (Exception e) {
            logger.error("Error retrieving user {} from database", userId, e);
            return null;
        }
    }
    
    /**
     * Get all groups from ACT_ID_GROUP table
     */
    public List<GroupDTO> getAllGroups() {
        try {
            String sql = "SELECT ID_, NAME_, TYPE_ FROM ACT_ID_GROUP ORDER BY ID_";
            
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            List<GroupDTO> groups = new ArrayList<>();
            
            for (Map<String, Object> row : rows) {
                String groupId = (String) row.get("ID_");
                String name = (String) row.get("NAME_");
                String type = (String) row.get("TYPE_");
                
                // Get users in this group
                List<String> userIds = getGroupUsers(groupId);
                
                groups.add(new GroupDTO(groupId, name, type, userIds));
            }
            
            logger.info("Retrieved {} groups from database", groups.size());
            return groups;
        } catch (Exception e) {
            logger.error("Error retrieving groups from database", e);
            throw new RuntimeException("Failed to retrieve groups", e);
        }
    }
    
    /**
     * Get a specific group by ID
     */
    public GroupDTO getGroupById(String groupId) {
        try {
            String sql = "SELECT ID_, NAME_, TYPE_ FROM ACT_ID_GROUP WHERE ID_ = ?";
            
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, groupId);
            
            String name = (String) row.get("NAME_");
            String type = (String) row.get("TYPE_");
            
            // Get users in this group
            List<String> userIds = getGroupUsers(groupId);
            
            return new GroupDTO(groupId, name, type, userIds);
        } catch (Exception e) {
            logger.error("Error retrieving group {} from database", groupId, e);
            return null;
        }
    }
    
    /**
     * Get groups for a specific user
     */
    public List<String> getUserGroups(String userId) {
        try {
            String sql = "SELECT GROUP_ID_ FROM ACT_ID_MEMBERSHIP WHERE USER_ID_ = ? ORDER BY GROUP_ID_";
            
            List<String> groups = jdbcTemplate.queryForList(sql, String.class, userId);
            return groups != null ? groups : new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error retrieving groups for user {}", userId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get users in a specific group
     */
    public List<String> getGroupUsers(String groupId) {
        try {
            String sql = "SELECT USER_ID_ FROM ACT_ID_MEMBERSHIP WHERE GROUP_ID_ = ? ORDER BY USER_ID_";
            
            List<String> users = jdbcTemplate.queryForList(sql, String.class, groupId);
            return users != null ? users : new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error retrieving users for group {}", groupId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get users by group ID
     */
    public List<UserDTO> getUsersByGroup(String groupId) {
        try {
            String sql = """
                SELECT u.ID_, u.FIRST_, u.LAST_, u.EMAIL_
                FROM ACT_ID_USER u
                INNER JOIN ACT_ID_MEMBERSHIP m ON u.ID_ = m.USER_ID_
                WHERE m.GROUP_ID_ = ?
                ORDER BY u.ID_
                """;
            
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, groupId);
            List<UserDTO> users = new ArrayList<>();
            
            for (Map<String, Object> row : rows) {
                String userId = (String) row.get("ID_");
                String firstName = (String) row.get("FIRST_");
                String lastName = (String) row.get("LAST_");
                String email = (String) row.get("EMAIL_");
                
                // Get all groups for this user
                List<String> groups = getUserGroups(userId);
                
                users.add(new UserDTO(userId, firstName, lastName, email, groups));
            }
            
            logger.info("Retrieved {} users for group {}", users.size(), groupId);
            return users;
        } catch (Exception e) {
            logger.error("Error retrieving users for group {}", groupId, e);
            throw new RuntimeException("Failed to retrieve users for group", e);
        }
    }
}

