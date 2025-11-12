package com.layoff.camunda_service.dtos;

import java.util.List;

public record GroupDTO(
    String id,
    String name,
    String type,
    List<String> userIds
) {}



