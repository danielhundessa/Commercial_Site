package com.layoff.user_service.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@Entity(name="users")
public class User {
    @Id
    private String id;
    private String keycloakId;
    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;
    private String phone;
    private UserRole role = UserRole.CUSTOMER;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Address address;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
