package com.layoff.camunda_service.controllers;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/camunda/process-instances")
@RequiredArgsConstructor
public class ProcessInstanceController {
    
    private final RuntimeService runtimeService;
    
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getProcessInstances(
            @RequestParam(required = false) String processDefinitionKey) {
        
        List<ProcessInstance> instances;
        
        if (processDefinitionKey != null) {
            instances = runtimeService.createProcessInstanceQuery()
                    .processDefinitionKey(processDefinitionKey)
                    .active()
                    .list();
        } else {
            instances = runtimeService.createProcessInstanceQuery()
                    .active()
                    .list();
        }
        
        List<Map<String, Object>> result = instances.stream()
                .map(instance -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", instance.getId());
                    map.put("processDefinitionId", instance.getProcessDefinitionId());
                    map.put("businessKey", instance.getBusinessKey());
                    map.put("variables", runtimeService.getVariables(instance.getId()));
                    return map;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{processInstanceId}")
    public ResponseEntity<Map<String, Object>> getProcessInstance(@PathVariable String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        
        if (instance == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", instance.getId());
        result.put("processDefinitionId", instance.getProcessDefinitionId());
        result.put("businessKey", instance.getBusinessKey());
        result.put("variables", runtimeService.getVariables(processInstanceId));
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{processInstanceId}/variables")
    public ResponseEntity<Map<String, Object>> getProcessInstanceVariables(@PathVariable String processInstanceId) {
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        return ResponseEntity.ok(variables);
    }
}

