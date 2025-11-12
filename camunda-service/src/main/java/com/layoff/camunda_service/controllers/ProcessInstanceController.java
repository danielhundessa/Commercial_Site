package com.layoff.camunda_service.controllers;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.runtime.ActivityInstance;
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
    private final HistoryService historyService;
    
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
        
        // Get process status (completed and active activities)
        Map<String, Object> status = getProcessStatusInternal(processInstanceId);
        result.put("status", status);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get process status showing completed and active activities
     */
    @GetMapping("/{processInstanceId}/status")
    public ResponseEntity<Map<String, Object>> getProcessStatus(@PathVariable String processInstanceId) {
        Map<String, Object> status = getProcessStatusInternal(processInstanceId);
        return ResponseEntity.ok(status);
    }
    
    private Map<String, Object> getProcessStatusInternal(String processInstanceId) {
        Map<String, Object> status = new HashMap<>();
        
        // Get completed activities from history
        List<HistoricActivityInstance> completedActivities = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .orderByHistoricActivityInstanceEndTime()
                .asc()
                .list();
        
        List<Map<String, Object>> completed = completedActivities.stream()
                .map(activity -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("activityId", activity.getActivityId());
                    map.put("activityName", activity.getActivityName());
                    map.put("activityType", activity.getActivityType());
                    map.put("startTime", activity.getStartTime());
                    map.put("endTime", activity.getEndTime());
                    map.put("duration", activity.getDurationInMillis());
                    return map;
                })
                .collect(Collectors.toList());
        
        status.put("completedActivities", completed);
        
        // Get active activities from runtime
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        
        List<Map<String, Object>> active = new java.util.ArrayList<>();
        if (instance != null && !instance.isEnded()) {
            ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);
            if (activityInstance != null) {
                collectActiveActivities(activityInstance, active);
            }
        }
        
        status.put("activeActivities", active);
        status.put("isEnded", instance == null || instance.isEnded());
        
        return status;
    }
    
    private void collectActiveActivities(ActivityInstance activityInstance, List<Map<String, Object>> active) {
        if (activityInstance.getActivityType() != null && 
            !activityInstance.getActivityType().equals("subProcess")) {
            Map<String, Object> map = new HashMap<>();
            map.put("activityId", activityInstance.getActivityId());
            map.put("activityName", activityInstance.getActivityName());
            map.put("activityType", activityInstance.getActivityType());
            active.add(map);
        }
        
        if (activityInstance.getChildActivityInstances() != null) {
            for (ActivityInstance child : activityInstance.getChildActivityInstances()) {
                collectActiveActivities(child, active);
            }
        }
    }
    
    @GetMapping("/{processInstanceId}/variables")
    public ResponseEntity<Map<String, Object>> getProcessInstanceVariables(@PathVariable String processInstanceId) {
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        return ResponseEntity.ok(variables);
    }
}

