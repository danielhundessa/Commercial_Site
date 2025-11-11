package com.layoff.camunda_service.controllers;

import com.layoff.camunda_service.dtos.TaskDTO;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/camunda/tasks")
@RequiredArgsConstructor
public class TaskController {
    
    private final TaskService taskService;
    
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getTasks(
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String candidateGroup) {
        
        List<Task> tasks;
        
        if (assignee != null) {
            tasks = taskService.createTaskQuery()
                    .taskAssignee(assignee)
                    .list();
        } else if (candidateGroup != null) {
            tasks = taskService.createTaskQuery()
                    .taskCandidateGroup(candidateGroup)
                    .list();
        } else {
            tasks = taskService.createTaskQuery()
                    .active()
                    .list();
        }
        
        List<TaskDTO> taskDTOs = tasks.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(taskDTOs);
    }
    
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(mapToDTO(task));
    }
    
    @GetMapping("/{taskId}/variables")
    public ResponseEntity<Map<String, Object>> getTaskVariables(@PathVariable String taskId) {
        Map<String, Object> variables = taskService.getVariables(taskId);
        return ResponseEntity.ok(variables);
    }
    
    @PostMapping("/{taskId}/claim")
    public ResponseEntity<Void> claimTask(
            @PathVariable String taskId,
            @RequestParam String userId) {
        
        taskService.claim(taskId, userId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Void> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> variables) {
        
        if (variables != null && !variables.isEmpty()) {
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{taskId}/unclaim")
    public ResponseEntity<Void> unclaimTask(@PathVariable String taskId) {
        taskService.setAssignee(taskId, null);
        return ResponseEntity.ok().build();
    }
    
    private TaskDTO mapToDTO(Task task) {
        Map<String, Object> variables = taskService.getVariables(task.getId());
        
        LocalDateTime created = task.getCreateTime() != null
                ? LocalDateTime.ofInstant(task.getCreateTime().toInstant(), ZoneId.systemDefault())
                : null;
        
        LocalDateTime due = task.getDueDate() != null
                ? LocalDateTime.ofInstant(task.getDueDate().toInstant(), ZoneId.systemDefault())
                : null;
        
        return new TaskDTO(
                task.getId(),
                task.getName(),
                task.getAssignee(),
                task.getProcessInstanceId(),
                task.getProcessDefinitionId(),
                task.getTaskDefinitionKey(),
                variables,
                created,
                due
        );
    }
}

