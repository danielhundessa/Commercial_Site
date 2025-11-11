package com.layoff.camunda_service.controllers;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceController.class);
    
    private final RuntimeService runtimeService;
    
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getProcessInstances(
            @RequestParam(required = false) String processDefinitionKey) {
        
        logger.info("=== PROCESS INSTANCE QUERY REQUEST ===");
        logger.info("Query parameter - processDefinitionKey: {}", processDefinitionKey);
        
        List<ProcessInstance> instances;
        
        if (processDefinitionKey != null) {
            logger.info("Querying process instances by processDefinitionKey: {}", processDefinitionKey);
            instances = runtimeService.createProcessInstanceQuery()
                    .processDefinitionKey(processDefinitionKey)
                    .active()
                    .list();
        } else {
            logger.info("Querying all active process instances");
            instances = runtimeService.createProcessInstanceQuery()
                    .active()
                    .list();
        }
        
        logger.info("Found {} active process instance(s)", instances.size());
        for (ProcessInstance instance : instances) {
            logger.info("ProcessInstance - Id: {}, ProcessDefinitionId: {}, BusinessKey: {}", 
                    instance.getId(), instance.getProcessDefinitionId(), instance.getBusinessKey());
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
        
        logger.info("=== PROCESS INSTANCE QUERY COMPLETED ===");
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

