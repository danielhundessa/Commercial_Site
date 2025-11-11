import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { camundaApi, TaskDTO } from '../services/camundaApi';
import './TaskDetail.css';

const TaskDetail: React.FC = () => {
  const { taskId } = useParams<{ taskId: string }>();
  const navigate = useNavigate();
  const [task, setTask] = useState<TaskDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [completing, setCompleting] = useState(false);
  const [formData, setFormData] = useState<Record<string, any>>({});
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    if (taskId) {
      loadTask();
    }
  }, [taskId]);

  const loadTask = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await camundaApi.getTask(taskId!);
      setTask(data);
      setFormData(data.variables || {});
    } catch (err: any) {
      setError(err.message || 'Failed to load task');
    } finally {
      setLoading(false);
    }
  };

  const handleComplete = async () => {
    if (!taskId) return;

    try {
      setCompleting(true);
      setError(null);
      setMessage(null);

      // Determine which variables to set based on task type
      const variables: Record<string, any> = { ...formData };

      if (task?.taskDefinitionKey === 'UserTask_ReviewOrder') {
        variables.reviewApproved = formData.reviewApproved === 'true' || formData.reviewApproved === true;
        if (formData.reviewComments) {
          variables.reviewComments = formData.reviewComments;
        }
      } else if (task?.taskDefinitionKey === 'UserTask_PaymentApproval') {
        variables.paymentApproved = formData.paymentApproved === 'true' || formData.paymentApproved === true;
        if (formData.paymentComments) {
          variables.paymentComments = formData.paymentComments;
        }
      } else if (task?.taskDefinitionKey === 'UserTask_PrepareShipping') {
        variables.shippingPrepared = true;
        if (formData.trackingNumber) {
          variables.trackingNumber = formData.trackingNumber;
        }
      } else if (task?.taskDefinitionKey === 'UserTask_ConfirmDelivery') {
        variables.deliveryConfirmed = true;
        if (formData.deliveryNotes) {
          variables.deliveryNotes = formData.deliveryNotes;
        }
      }

      await camundaApi.completeTask(taskId, variables);
      setMessage('Task completed successfully!');
      setTimeout(() => {
        navigate('/');
      }, 2000);
    } catch (err: any) {
      setError(err.message || 'Failed to complete task');
    } finally {
      setCompleting(false);
    }
  };

  const handleInputChange = (key: string, value: any) => {
    setFormData((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  if (loading) {
    return <div className="loading">Loading task details...</div>;
  }

  if (error && !task) {
    return (
      <div className="error-container">
        <div className="error">{error}</div>
        <button className="btn btn-secondary" onClick={() => navigate('/')}>
          Back to Tasks
        </button>
      </div>
    );
  }

  if (!task) {
    return <div className="loading">Task not found</div>;
  }

  return (
    <div className="task-detail">
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">{task.name}</h2>
          <button className="btn btn-secondary" onClick={() => navigate('/')}>
            Back to Tasks
          </button>
        </div>

        {message && <div className="success">{message}</div>}
        {error && <div className="error">{error}</div>}

        <div className="task-info">
          <div className="info-row">
            <span className="info-label">Task ID:</span>
            <span className="info-value">{task.id}</span>
          </div>
          <div className="info-row">
            <span className="info-label">Assignee:</span>
            <span className="info-value">{task.assignee || 'Unassigned'}</span>
          </div>
          <div className="info-row">
            <span className="info-label">Process Instance ID:</span>
            <span className="info-value">{task.processInstanceId}</span>
          </div>
          <div className="info-row">
            <span className="info-label">Task Definition Key:</span>
            <span className="info-value">{task.taskDefinitionKey}</span>
          </div>
          <div className="info-row">
            <span className="info-label">Created:</span>
            <span className="info-value">
              {task.created ? new Date(task.created).toLocaleString() : 'N/A'}
            </span>
          </div>
        </div>

        <div className="task-form">
          <h3 className="form-title">Task Form</h3>

          {task.taskDefinitionKey === 'UserTask_ReviewOrder' && (
            <div>
              <div className="form-group">
                <label className="form-label">Approve Order:</label>
                <select
                  className="form-select"
                  value={formData.reviewApproved || ''}
                  onChange={(e) => handleInputChange('reviewApproved', e.target.value)}
                >
                  <option value="">Select...</option>
                  <option value="true">Approve</option>
                  <option value="false">Reject</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Review Comments:</label>
                <textarea
                  className="form-textarea"
                  value={formData.reviewComments || ''}
                  onChange={(e) => handleInputChange('reviewComments', e.target.value)}
                  placeholder="Enter review comments..."
                />
              </div>
            </div>
          )}

          {task.taskDefinitionKey === 'UserTask_PaymentApproval' && (
            <div>
              <div className="form-group">
                <label className="form-label">Approve Payment:</label>
                <select
                  className="form-select"
                  value={formData.paymentApproved || ''}
                  onChange={(e) => handleInputChange('paymentApproved', e.target.value)}
                >
                  <option value="">Select...</option>
                  <option value="true">Approve</option>
                  <option value="false">Reject</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Payment Comments:</label>
                <textarea
                  className="form-textarea"
                  value={formData.paymentComments || ''}
                  onChange={(e) => handleInputChange('paymentComments', e.target.value)}
                  placeholder="Enter payment comments..."
                />
              </div>
            </div>
          )}

          {task.taskDefinitionKey === 'UserTask_PrepareShipping' && (
            <div>
              <div className="form-group">
                <label className="form-label">Tracking Number:</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.trackingNumber || ''}
                  onChange={(e) => handleInputChange('trackingNumber', e.target.value)}
                  placeholder="Enter tracking number..."
                />
              </div>
            </div>
          )}

          {task.taskDefinitionKey === 'UserTask_ConfirmDelivery' && (
            <div>
              <div className="form-group">
                <label className="form-label">Delivery Notes:</label>
                <textarea
                  className="form-textarea"
                  value={formData.deliveryNotes || ''}
                  onChange={(e) => handleInputChange('deliveryNotes', e.target.value)}
                  placeholder="Enter delivery notes..."
                />
              </div>
            </div>
          )}

          <div className="form-actions">
            <button
              className="btn btn-success"
              onClick={handleComplete}
              disabled={completing}
            >
              {completing ? 'Completing...' : 'Complete Task'}
            </button>
          </div>
        </div>

        <div className="variables-display">
          <h3 className="form-title">Process Variables</h3>
          <pre>{JSON.stringify(task.variables, null, 2)}</pre>
        </div>
      </div>
    </div>
  );
};

export default TaskDetail;

