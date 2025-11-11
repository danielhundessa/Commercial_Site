package com.layoff.camunda_service.dtos;

import java.util.Map;

public record TaskDTO(
    String id,
    String name,
    String assignee,
    String processInstanceId,
    String processDefinitionId,
    String taskDefinitionKey,
    Map<String, Object> variables,
    java.time.LocalDateTime created,
    java.time.LocalDateTime due
) {}

