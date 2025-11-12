package com.layoff.camunda_service.controllers;

import com.layoff.camunda_service.dtos.TaskDTO;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    
    private final TaskService taskService;
    
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getTasks(
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String candidateGroup,
            @RequestParam(required = false) String processInstanceId) {
        
        logger.info("=== TASK QUERY REQUEST ===");
        logger.info("Query parameters - assignee: {}, candidateGroup: {}, processInstanceId: {}", 
                assignee, candidateGroup, processInstanceId);
        
        List<Task> tasks;
        
        if (processInstanceId != null) {
            logger.info("Querying tasks by processInstanceId: {}", processInstanceId);
            tasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .active()
                    .list();
        } else if (assignee != null) {
            logger.info("Querying tasks by assignee: {}", assignee);
            tasks = taskService.createTaskQuery()
                    .taskAssignee(assignee)
                    .list();
        } else if (candidateGroup != null) {
            logger.info("Querying tasks by candidateGroup: {}", candidateGroup);
            tasks = taskService.createTaskQuery()
                    .taskCandidateGroup(candidateGroup)
                    .list();
        } else {
            logger.info("Querying all active tasks");
            tasks = taskService.createTaskQuery()
                    .active()
                    .list();
        }
        
        logger.info("Found {} task(s)", tasks.size());
        for (Task task : tasks) {
            logger.info("Task - Id: {}, Name: {}, Assignee: {}, ProcessInstanceId: {}, TaskDefinitionKey: {}", 
                    task.getId(), task.getName(), task.getAssignee(), 
                    task.getProcessInstanceId(), task.getTaskDefinitionKey());
        }
        
        List<TaskDTO> taskDTOs = tasks.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        logger.info("=== TASK QUERY COMPLETED ===");
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


