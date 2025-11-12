import React, { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { camundaApi, TaskDTO, UserDTO, ProcessStatus } from '../services/camundaApi';
import './TaskList.css';

const TaskList: React.FC = () => {
  const [searchParams] = useSearchParams();
  const [tasks, setTasks] = useState<TaskDTO[]>([]);
  const [users, setUsers] = useState<UserDTO[]>([]);
  const [processStatuses, setProcessStatuses] = useState<Record<string, ProcessStatus>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterGroup, setFilterGroup] = useState<string>('');
  const [userId, setUserId] = useState<string>('');
  const [assigning, setAssigning] = useState<Record<string, boolean>>({});
  const [filterProcessInstanceId, setFilterProcessInstanceId] = useState<string>('');

  useEffect(() => {
    // Get processInstanceId from URL params
    const processInstanceId = searchParams.get('processInstanceId');
    if (processInstanceId) {
      setFilterProcessInstanceId(processInstanceId);
    }
    loadUsers();
  }, [searchParams]);

  useEffect(() => {
    loadTasks();
  }, [filterGroup, userId, filterProcessInstanceId]);

  const loadUsers = async () => {
    try {
      const data = await camundaApi.getAllUsers();
      setUsers(data);
    } catch (err) {
      console.error('Failed to load users:', err);
    }
  };

  const loadTasks = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await camundaApi.getTasks(
        userId || undefined,
        filterGroup || undefined,
        filterProcessInstanceId || undefined
      );
      setTasks(data);
      
      // Load process status for each task
      const statusPromises = data.map(async (task) => {
        try {
          const status = await camundaApi.getProcessStatus(task.processInstanceId);
          return { processInstanceId: task.processInstanceId, status };
        } catch (err) {
          console.error(`Failed to load status for ${task.processInstanceId}:`, err);
          return { processInstanceId: task.processInstanceId, status: null };
        }
      });
      
      const statusResults = await Promise.all(statusPromises);
      const statusMap: Record<string, ProcessStatus> = {};
      statusResults.forEach(({ processInstanceId, status }) => {
        if (status) {
          statusMap[processInstanceId] = status;
        }
      });
      setProcessStatuses(statusMap);
    } catch (err: any) {
      setError(err.message || 'Failed to load tasks');
    } finally {
      setLoading(false);
    }
  };

  const handleClaim = async (taskId: string, selectedUserId?: string) => {
    const userToUse = selectedUserId || userId;
    if (!userToUse) {
      alert('Please select a user to claim tasks');
      return;
    }
    
    try {
      setAssigning({ ...assigning, [taskId]: true });
      await camundaApi.claimTask(taskId, userToUse);
      await loadTasks();
    } catch (err: any) {
      alert('Failed to claim task: ' + err.message);
    } finally {
      setAssigning({ ...assigning, [taskId]: false });
    }
  };
  
  const handleAssign = async (taskId: string, selectedUserId: string) => {
    try {
      setAssigning({ ...assigning, [taskId]: true });
      await camundaApi.claimTask(taskId, selectedUserId);
      await loadTasks();
    } catch (err: any) {
      alert('Failed to assign task: ' + err.message);
    } finally {
      setAssigning({ ...assigning, [taskId]: false });
    }
  };

  const handleUnclaim = async (taskId: string) => {
    try {
      await camundaApi.unclaimTask(taskId);
      await loadTasks();
    } catch (err: any) {
      alert('Failed to unclaim task: ' + err.message);
    }
  };

  if (loading) {
    return <div className="loading">Loading tasks...</div>;
  }

  return (
    <div className="task-list">
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">
            Tasks
            {filterProcessInstanceId && (
              <span style={{ fontSize: '0.9rem', marginLeft: '1rem', color: '#666' }}>
                (Filtered by Process Instance: {filterProcessInstanceId.substring(0, 8)}...)
                <button
                  className="btn btn-secondary btn-sm"
                  onClick={() => {
                    setFilterProcessInstanceId('');
                    window.history.pushState({}, '', '/');
                  }}
                  style={{ marginLeft: '0.5rem' }}
                >
                  Clear Filter
                </button>
              </span>
            )}
          </h2>
          <button className="btn btn-primary" onClick={loadTasks}>
            Refresh
          </button>
        </div>

        {error && <div className="error">{error}</div>}

        <div className="filters">
          <div className="form-group">
            <label className="form-label">Select User (for claiming tasks):</label>
            <select
              className="form-select"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
            >
              <option value="">-- Select User --</option>
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.firstName} {user.lastName} ({user.id}) - {user.groups.join(', ')}
                </option>
              ))}
            </select>
            {users.length === 0 && (
              <small style={{ color: '#666', display: 'block', marginTop: '0.25rem' }}>
                Loading users...
              </small>
            )}
          </div>
          <div className="form-group">
            <label className="form-label">Filter by Candidate Group:</label>
            <select
              className="form-select"
              value={filterGroup}
              onChange={(e) => setFilterGroup(e.target.value)}
            >
              <option value="">All Groups</option>
              <option value="order_managers">Order Managers</option>
              <option value="finance_team">Finance Team</option>
              <option value="warehouse_team">Warehouse Team</option>
              <option value="delivery_team">Delivery Team</option>
            </select>
          </div>
        </div>

        {tasks.length === 0 ? (
          <div className="no-tasks">No tasks found</div>
        ) : (
          <div className="tasks-list">
            {tasks.map((task) => {
              const status = processStatuses[task.processInstanceId];
              const isAssigning = assigning[task.id];
              
              return (
                <div key={task.id} className="task-card" style={{
                  border: '1px solid #ddd',
                  borderRadius: '8px',
                  padding: '1rem',
                  marginBottom: '1rem',
                  backgroundColor: '#fff'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                    <div style={{ flex: 1 }}>
                      <h3 style={{ margin: '0 0 0.5rem 0', fontSize: '1.1rem' }}>
                        <Link to={`/task/${task.id}`} style={{ textDecoration: 'none', color: '#007bff' }}>
                          {task.name}
                        </Link>
                      </h3>
                      <div style={{ fontSize: '0.9rem', color: '#666', marginBottom: '0.5rem' }}>
                        <div><strong>Task ID:</strong> {task.id.substring(0, 8)}...</div>
                        <div><strong>Process Instance:</strong> {task.processInstanceId.substring(0, 8)}...</div>
                        <div><strong>Order ID:</strong> {task.variables?.orderId || 'N/A'}</div>
                        <div><strong>User ID:</strong> {task.variables?.userId || 'N/A'}</div>
                        <div><strong>Total Amount:</strong> ${task.variables?.totalAmount || 'N/A'}</div>
                        <div><strong>Assignee:</strong> {task.assignee || <span style={{ color: '#ffc107', fontWeight: 'bold' }}>Unassigned</span>}</div>
                        <div><strong>Created:</strong> {task.created ? new Date(task.created).toLocaleString() : 'N/A'}</div>
                      </div>
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', minWidth: '200px' }}>
                      {!task.assignee ? (
                        <div>
                          <label style={{ fontSize: '0.85rem', display: 'block', marginBottom: '0.25rem' }}>Assign to:</label>
                          <select
                            className="form-select"
                            value={userId}
                            onChange={(e) => {
                              const selectedUserId = e.target.value;
                              setUserId(selectedUserId);
                              if (selectedUserId) {
                                handleAssign(task.id, selectedUserId);
                              }
                            }}
                            disabled={isAssigning}
                            style={{ marginBottom: '0.5rem' }}
                          >
                            <option value="">-- Select User --</option>
                            {users.map((user) => (
                              <option key={user.id} value={user.id}>
                                {user.firstName} {user.lastName} ({user.id})
                              </option>
                            ))}
                          </select>
                          <button
                            className="btn btn-primary btn-sm"
                            onClick={() => handleClaim(task.id)}
                            disabled={!userId || isAssigning}
                            style={{ width: '100%' }}
                          >
                            {isAssigning ? 'Assigning...' : 'Claim Task'}
                          </button>
                        </div>
                      ) : (
                        <div>
                          <div style={{ fontSize: '0.85rem', marginBottom: '0.5rem', color: '#28a745' }}>
                            ✓ Assigned to: <strong>{task.assignee}</strong>
                          </div>
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => handleUnclaim(task.id)}
                            style={{ width: '100%', marginBottom: '0.5rem' }}
                          >
                            Unclaim
                          </button>
                          <Link to={`/task/${task.id}`} className="btn btn-success btn-sm" style={{ display: 'block', textAlign: 'center', textDecoration: 'none' }}>
                            Complete Task
                          </Link>
                        </div>
                      )}
                    </div>
                  </div>
                  
                  {/* Process Status Indicator */}
                  {status && (
                    <div style={{ marginTop: '1rem', paddingTop: '1rem', borderTop: '2px solid #007bff' }}>
                      <ProcessStatusDisplay status={status} />
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

// Component to display process status with visual indicators
const ProcessStatusDisplay: React.FC<{ status: ProcessStatus }> = ({ status }) => {
  // Define the process flow steps in order
  const processSteps = [
    { id: 'StartEvent_OrderReceived', name: 'Order Received', type: 'startEvent' },
    { id: 'ServiceTask_ValidateOrder', name: 'Validate Order', type: 'serviceTask' },
    { id: 'UserTask_ReviewOrder', name: 'Review Order', type: 'userTask' },
    { id: 'Gateway_ReviewDecision', name: 'Review Decision', type: 'gateway' },
    { id: 'UserTask_PaymentApproval', name: 'Approve Payment', type: 'userTask' },
    { id: 'Gateway_PaymentDecision', name: 'Payment Decision', type: 'gateway' },
    { id: 'ServiceTask_ProcessPayment', name: 'Process Payment', type: 'serviceTask' },
    { id: 'UserTask_PrepareShipping', name: 'Prepare Shipping', type: 'userTask' },
    { id: 'ServiceTask_ShipOrder', name: 'Ship Order', type: 'serviceTask' },
    { id: 'UserTask_ConfirmDelivery', name: 'Confirm Delivery', type: 'userTask' },
    { id: 'EndEvent_OrderCompleted', name: 'Order Completed', type: 'endEvent' },
  ];

  const completedIds = new Set(status.completedActivities.map(a => a.activityId));
  const activeIds = new Set(status.activeActivities.map(a => a.activityId));

  // Find current step
  const currentStep = status.activeActivities.length > 0 
    ? status.activeActivities[0] 
    : (status.completedActivities.length > 0 
        ? status.completedActivities[status.completedActivities.length - 1] 
        : null);

  // Calculate progress percentage
  const totalSteps = processSteps.length;
  const completedSteps = status.completedActivities.length;
  const progressPercentage = status.isEnded ? 100 : Math.round((completedSteps / totalSteps) * 100);

  return (
    <div>
      <div style={{ marginBottom: '1rem', padding: '1rem', backgroundColor: '#f8f9fa', borderRadius: '8px', border: '1px solid #dee2e6' }}>
        <h4 style={{ margin: '0 0 0.5rem 0', fontSize: '1.1rem', color: '#007bff' }}>📊 Order Progress</h4>
        <div style={{ marginBottom: '0.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.25rem' }}>
            <span style={{ fontSize: '0.9rem' }}>Progress: {completedSteps} of {totalSteps} steps completed</span>
            <span style={{ fontSize: '0.9rem', fontWeight: 'bold', color: '#007bff' }}>{progressPercentage}%</span>
          </div>
          <div style={{ width: '100%', height: '20px', backgroundColor: '#e9ecef', borderRadius: '10px', overflow: 'hidden' }}>
            <div 
              style={{ 
                width: `${progressPercentage}%`, 
                height: '100%', 
                backgroundColor: status.isEnded ? '#28a745' : '#007bff',
                transition: 'width 0.3s ease',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#fff',
                fontSize: '0.75rem',
                fontWeight: 'bold'
              }}
            >
              {progressPercentage}%
            </div>
          </div>
        </div>
        {currentStep && (
          <div style={{ fontSize: '0.9rem', color: '#666', marginTop: '0.5rem' }}>
            <strong>Current Step:</strong> <span style={{ color: '#007bff', fontWeight: 'bold' }}>{currentStep.activityName}</span>
          </div>
        )}
      </div>
      
      <h4 style={{ marginBottom: '1rem', fontSize: '1rem' }}>Process Flow Status</h4>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', alignItems: 'center', marginBottom: '1rem' }}>
        {processSteps.map((step, index) => {
          const isCompleted = completedIds.has(step.id);
          const isActive = activeIds.has(step.id);
          const isPending = !isCompleted && !isActive;
          
          let statusColor = '#ccc'; // Pending - gray
          let statusIcon = '○';
          let statusText = 'Pending';
          
          if (isCompleted) {
            statusColor = '#28a745'; // Completed - green
            statusIcon = '✓';
            statusText = 'Completed';
          } else if (isActive) {
            statusColor = '#007bff'; // Active - blue
            statusIcon = '⟳';
            statusText = 'Active';
          }
          
          return (
            <React.Fragment key={step.id}>
              <div
                style={{
                  padding: '0.5rem 1rem',
                  borderRadius: '6px',
                  backgroundColor: statusColor,
                  color: isPending ? '#666' : '#fff',
                  fontSize: '0.85rem',
                  fontWeight: isActive ? 'bold' : 'normal',
                  minWidth: '120px',
                  textAlign: 'center',
                  boxShadow: isActive ? '0 0 8px rgba(0,123,255,0.5)' : 'none',
                }}
                title={statusText}
              >
                <div style={{ fontSize: '1.2rem', marginBottom: '0.25rem' }}>{statusIcon}</div>
                <div>{step.name}</div>
              </div>
              {index < processSteps.length - 1 && (
                <div style={{ color: '#ccc', fontSize: '1.2rem' }}>→</div>
              )}
            </React.Fragment>
          );
        })}
      </div>
      
      <div style={{ marginTop: '1.5rem', fontSize: '0.9rem' }}>
        <div style={{ marginBottom: '0.5rem' }}>
          <strong>Completed Steps ({status.completedActivities.length}):</strong>
          <ul style={{ margin: '0.5rem 0', paddingLeft: '1.5rem' }}>
            {status.completedActivities.map((activity, idx) => (
              <li key={idx}>
                {activity.activityName}
                {activity.endTime && (
                  <span style={{ color: '#666', fontSize: '0.85rem' }}>
                    {' '}({new Date(activity.endTime).toLocaleString()})
                  </span>
                )}
              </li>
            ))}
          </ul>
        </div>
        
        {status.activeActivities.length > 0 && (
          <div>
            <strong>Active Steps ({status.activeActivities.length}):</strong>
            <ul style={{ margin: '0.5rem 0', paddingLeft: '1.5rem' }}>
              {status.activeActivities.map((activity, idx) => (
                <li key={idx} style={{ color: '#007bff', fontWeight: 'bold' }}>
                  {activity.activityName}
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
};

export default TaskList;




