package com.layoff.user_service.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
