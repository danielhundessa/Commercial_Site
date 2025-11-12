package com.layoff.camunda_service.dtos;

import java.util.List;

public record UserDTO(
    String id,
    String firstName,
    String lastName,
    String email,
    List<String> groups
) {}


