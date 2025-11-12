import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_CAMUNDA_API_URL || 'http://localhost:5050/api/camunda';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export interface TaskDTO {
  id: string;
  name: string;
  assignee: string | null;
  processInstanceId: string;
  processDefinitionId: string;
  taskDefinitionKey: string;
  variables: Record<string, any>;
  created: string | null;
  due: string | null;
}

export interface ProcessActivity {
  activityId: string;
  activityName: string;
  activityType: string;
  startTime?: string;
  endTime?: string;
  duration?: number;
}

export interface ProcessStatus {
  completedActivities: ProcessActivity[];
  activeActivities: ProcessActivity[];
  isEnded: boolean;
}

export interface ProcessInstance {
  id: string;
  processDefinitionId: string;
  businessKey: string | null;
  variables: Record<string, any>;
  status?: ProcessStatus;
}

export const camundaApi = {
  // Task operations
  getTasks: async (assignee?: string, candidateGroup?: string, processInstanceId?: string): Promise<TaskDTO[]> => {
    const params: any = {};
    if (assignee) params.assignee = assignee;
    if (candidateGroup) params.candidateGroup = candidateGroup;
    if (processInstanceId) params.processInstanceId = processInstanceId;
    
    const response = await api.get<TaskDTO[]>('/tasks', { params });
    return response.data;
  },

  getTask: async (taskId: string): Promise<TaskDTO> => {
    const response = await api.get<TaskDTO>(`/tasks/${taskId}`);
    return response.data;
  },

  getTaskVariables: async (taskId: string): Promise<Record<string, any>> => {
    const response = await api.get<Record<string, any>>(`/tasks/${taskId}/variables`);
    return response.data;
  },

  claimTask: async (taskId: string, userId: string): Promise<void> => {
    await api.post(`/tasks/${taskId}/claim`, null, {
      params: { userId },
    });
  },

  completeTask: async (taskId: string, variables?: Record<string, any>): Promise<void> => {
    await api.post(`/tasks/${taskId}/complete`, variables || {});
  },

  unclaimTask: async (taskId: string): Promise<void> => {
    await api.post(`/tasks/${taskId}/unclaim`);
  },

  // Process instance operations
  getProcessInstances: async (processDefinitionKey?: string): Promise<ProcessInstance[]> => {
    const params: any = {};
    if (processDefinitionKey) params.processDefinitionKey = processDefinitionKey;
    
    const response = await api.get<ProcessInstance[]>('/process-instances', { params });
    return response.data;
  },

  getProcessInstance: async (processInstanceId: string): Promise<ProcessInstance> => {
    const response = await api.get<ProcessInstance>(`/process-instances/${processInstanceId}`);
    return response.data;
  },

  getProcessInstanceVariables: async (processInstanceId: string): Promise<Record<string, any>> => {
    const response = await api.get<Record<string, any>>(`/process-instances/${processInstanceId}/variables`);
    return response.data;
  },

  getProcessStatus: async (processInstanceId: string): Promise<ProcessStatus> => {
    const response = await api.get<ProcessStatus>(`/process-instances/${processInstanceId}/status`);
    return response.data;
  },

  // Identity operations
  getAllUsers: async (): Promise<UserDTO[]> => {
    const response = await api.get<UserDTO[]>('/identity/users');
    return response.data;
  },

  getUsersByGroup: async (groupId: string): Promise<UserDTO[]> => {
    const response = await api.get<UserDTO[]>(`/identity/groups/${groupId}/users`);
    return response.data;
  },
};

export interface UserDTO {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  groups: string[];
}




