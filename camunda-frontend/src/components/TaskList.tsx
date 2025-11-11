import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { camundaApi, TaskDTO } from '../services/camundaApi';
import './TaskList.css';

const TaskList: React.FC = () => {
  const [tasks, setTasks] = useState<TaskDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterGroup, setFilterGroup] = useState<string>('');
  const [userId, setUserId] = useState<string>('');

  useEffect(() => {
    loadTasks();
  }, [filterGroup, userId]);

  const loadTasks = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await camundaApi.getTasks(
        userId || undefined,
        filterGroup || undefined
      );
      setTasks(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load tasks');
    } finally {
      setLoading(false);
    }
  };

  const handleClaim = async (taskId: string) => {
    if (!userId) {
      alert('Please enter a user ID to claim tasks');
      return;
    }
    
    try {
      await camundaApi.claimTask(taskId, userId);
      await loadTasks();
    } catch (err: any) {
      alert('Failed to claim task: ' + err.message);
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
          <h2 className="card-title">Tasks</h2>
          <button className="btn btn-primary" onClick={loadTasks}>
            Refresh
          </button>
        </div>

        {error && <div className="error">{error}</div>}

        <div className="filters">
          <div className="form-group">
            <label className="form-label">User ID (for claiming tasks):</label>
            <input
              type="text"
              className="form-input"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              placeholder="Enter user ID"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Filter by Candidate Group:</label>
            <select
              className="form-select"
              value={filterGroup}
              onChange={(e) => setFilterGroup(e.target.value)}
            >
              <option value="">All Groups</option>
              <option value="order-managers">Order Managers</option>
              <option value="finance-team">Finance Team</option>
              <option value="warehouse-team">Warehouse Team</option>
              <option value="delivery-team">Delivery Team</option>
            </select>
          </div>
        </div>

        {tasks.length === 0 ? (
          <div className="no-tasks">No tasks found</div>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Task Name</th>
                <th>Assignee</th>
                <th>Process Instance</th>
                <th>Created</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {tasks.map((task) => (
                <tr key={task.id}>
                  <td>
                    <Link to={`/task/${task.id}`} className="task-link">
                      {task.name}
                    </Link>
                  </td>
                  <td>{task.assignee || <span className="badge badge-warning">Unassigned</span>}</td>
                  <td>{task.processInstanceId.substring(0, 8)}...</td>
                  <td>{task.created ? new Date(task.created).toLocaleString() : 'N/A'}</td>
                  <td>
                    <div className="action-buttons">
                      {!task.assignee ? (
                        <button
                          className="btn btn-primary btn-sm"
                          onClick={() => handleClaim(task.id)}
                          disabled={!userId}
                        >
                          Claim
                        </button>
                      ) : (
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => handleUnclaim(task.id)}
                        >
                          Unclaim
                        </button>
                      )}
                      <Link to={`/task/${task.id}`} className="btn btn-success btn-sm">
                        View
                      </Link>
                    </div>
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

export default TaskList;

