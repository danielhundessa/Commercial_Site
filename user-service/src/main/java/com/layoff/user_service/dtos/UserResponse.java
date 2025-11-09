package com.layoff.user_service.dtos;

import com.layoff.user_service.models.UserRole;
import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String keycloakId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private AddressDTO address;
    private UserRole role;
}
