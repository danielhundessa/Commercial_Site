import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { camundaApi, ProcessInstance, ProcessStatus } from '../services/camundaApi';

interface ProcessInstanceWithStatus extends ProcessInstance {
  currentStep?: string;
  isCompleted?: boolean;
}

const ProcessInstances: React.FC = () => {
  const [instances, setInstances] = useState<ProcessInstanceWithStatus[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    loadProcessInstances();
  }, []);

  const loadProcessInstances = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await camundaApi.getProcessInstances();
      
      // Load status for each instance to get current step
      const instancesWithStatus = await Promise.all(
        data.map(async (instance) => {
          try {
            const status = await camundaApi.getProcessStatus(instance.id);
            const currentStep = status.activeActivities.length > 0
              ? status.activeActivities[0].activityName
              : (status.completedActivities.length > 0
                  ? status.completedActivities[status.completedActivities.length - 1].activityName
                  : 'Not Started');
            
            return {
              ...instance,
              currentStep,
              isCompleted: status.isEnded
            };
          } catch (err) {
            console.error(`Failed to load status for ${instance.id}:`, err);
            return {
              ...instance,
              currentStep: 'Unknown',
              isCompleted: false
            };
          }
        })
      );
      
      setInstances(instancesWithStatus);
    } catch (err: any) {
      setError(err.message || 'Failed to load process instances');
    } finally {
      setLoading(false);
    }
  };

  const handleInstanceClick = (processInstanceId: string) => {
    // Navigate to tasks page with process instance filter
    navigate(`/?processInstanceId=${processInstanceId}`);
  };

  if (loading) {
    return <div className="loading">Loading process instances...</div>;
  }

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">Process Instances</h2>
          <button className="btn btn-primary" onClick={loadProcessInstances}>
            Refresh
          </button>
        </div>

        {error && <div className="error">{error}</div>}

        {instances.length === 0 ? (
          <div className="no-tasks">No active process instances found</div>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Process Instance ID</th>
                <th>Order ID</th>
                <th>User ID</th>
                <th>Total Amount</th>
                <th>Current Step</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {instances.map((instance) => (
                <tr 
                  key={instance.id}
                  style={{ cursor: 'pointer' }}
                  onClick={() => handleInstanceClick(instance.id)}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = '#f8f9fa';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = '#fff';
                  }}
                >
                  <td>{instance.id.substring(0, 16)}...</td>
                  <td>{instance.variables?.orderId || 'N/A'}</td>
                  <td>{instance.variables?.userId || 'N/A'}</td>
                  <td>${instance.variables?.totalAmount || 'N/A'}</td>
                  <td>
                    <span style={{ 
                      color: instance.isCompleted ? '#28a745' : '#007bff',
                      fontWeight: instance.isCompleted ? 'normal' : 'bold'
                    }}>
                      {instance.currentStep || 'Loading...'}
                    </span>
                  </td>
                  <td>
                    {instance.isCompleted ? (
                      <span style={{ color: '#28a745', fontWeight: 'bold' }}>✅ Completed</span>
                    ) : (
                      <span style={{ color: '#007bff', fontWeight: 'bold' }}>🔄 In Progress</span>
                    )}
                  </td>
                  <td>
                    <button
                      className="btn btn-primary btn-sm"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleInstanceClick(instance.id);
                      }}
                    >
                      View Tasks
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default ProcessInstances;




