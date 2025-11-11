package com.layoff.camunda_service;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

record OrderItemDTO(Long id, String productId, Integer quantity) {}
record OrderCreatedEvent(Long orderId, String userId, String status, java.util.List<OrderItemDTO> items, java.math.BigDecimal totalAmount, java.time.LocalDateTime createdAt) {}

@SpringBootApplication
public class CamundaServiceApplication {
	
	private static final Logger logger = LoggerFactory.getLogger(CamundaServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(CamundaServiceApplication.class, args);
		logger.info("=== CAMUNDA SERVICE APPLICATION STARTED ===");
	}

	@Bean
	public Consumer<OrderCreatedEvent> orderCreated(RuntimeService runtimeService, 
	                                                 RepositoryService repositoryService,
	                                                 TaskService taskService) {
		return event -> {
			logger.info("=== CAMUNDA EVENT CONSUMPTION STARTED ===");
			logger.info("Received OrderCreatedEvent from Kafka. OrderId: {}, UserId: {}, TotalAmount: {}, ItemsCount: {}", 
					event.orderId(), event.userId(), event.totalAmount(), 
					event.items() != null ? event.items().size() : 0);
			
			try {
				// Check if process definition exists
				logger.info("Checking if process definition 'order_process' exists...");
				ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
						.processDefinitionKey("order_process")
						.latestVersion()
						.singleResult();
				
				if (processDefinition == null) {
					logger.error("CRITICAL: Process definition 'order_process' NOT FOUND in repository!");
					logger.error("Available process definitions:");
					List<ProcessDefinition> allDefs = repositoryService.createProcessDefinitionQuery().list();
					for (ProcessDefinition def : allDefs) {
						logger.error("  - Key: {}, Id: {}, Name: {}, Version: {}", 
								def.getKey(), def.getId(), def.getName(), def.getVersion());
					}
					return;
				}
				
				logger.info("Process definition found. ProcessDefinitionId: {}, Key: {}, Name: {}, Version: {}", 
						processDefinition.getId(), processDefinition.getKey(), 
						processDefinition.getName(), processDefinition.getVersion());
				
				// Prepare process variables
				logger.info("Preparing process variables for OrderId: {}", event.orderId());
				Map<String, Object> vars = new HashMap<>();
				vars.put("orderId", event.orderId());
				vars.put("userId", event.userId());
				vars.put("totalAmount", event.totalAmount());
				logger.info("Process variables prepared. orderId: {}, userId: {}, totalAmount: {}", 
						event.orderId(), event.userId(), event.totalAmount());
				
				// Start process instance
				logger.info("Starting Camunda process instance with key 'order_process' for OrderId: {}", event.orderId());
				ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("order_process", vars);
				
				if (processInstance != null) {
					logger.info("Process instance created successfully! ProcessInstanceId: {}, BusinessKey: {}, ProcessDefinitionId: {}, OrderId: {}", 
							processInstance.getId(), 
							processInstance.getBusinessKey(),
							processInstance.getProcessDefinitionId(),
							event.orderId());
					
					// Log process variables
					Map<String, Object> processVars = runtimeService.getVariables(processInstance.getId());
					logger.info("Process instance variables: {}", processVars);
					
					// Check if process is active
					ProcessInstance activeInstance = runtimeService.createProcessInstanceQuery()
							.processInstanceId(processInstance.getId())
							.singleResult();
					if (activeInstance != null) {
						logger.info("Process instance is ACTIVE. ProcessInstanceId: {}", processInstance.getId());
						
						// Check for tasks
						List<Task> tasks = taskService.createTaskQuery()
								.processInstanceId(processInstance.getId())
								.list();
						logger.info("Found {} task(s) for ProcessInstanceId: {}", tasks.size(), processInstance.getId());
						for (Task task : tasks) {
							logger.info("Task - Id: {}, Name: {}, Assignee: {}, TaskDefinitionKey: {}",
									task.getId(), task.getName(), task.getAssignee(), task.getTaskDefinitionKey());
						}
					} else {
						logger.warn("Process instance is NOT ACTIVE (may have completed or failed). ProcessInstanceId: {}", processInstance.getId());
					}
				} else {
					logger.error("FAILED: Process instance is NULL after startProcessInstanceByKey for OrderId: {}", event.orderId());
				}
				
				logger.info("=== CAMUNDA EVENT CONSUMPTION COMPLETED ===");
			} catch (Exception e) {
				logger.error("ERROR: Exception occurred while processing OrderCreatedEvent for OrderId: {}", event.orderId(), e);
				logger.error("Exception type: {}, Message: {}", e.getClass().getName(), e.getMessage());
				if (e.getCause() != null) {
					logger.error("Root cause: {}", e.getCause().getMessage());
				}
				e.printStackTrace();
			}
		};
	}
}
