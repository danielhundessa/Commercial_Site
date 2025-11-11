import React, { useState, useEffect } from 'react';
import { camundaApi, ProcessInstance } from '../services/camundaApi';

const ProcessInstances: React.FC = () => {
  const [instances, setInstances] = useState<ProcessInstance[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadProcessInstances();
  }, []);

  const loadProcessInstances = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await camundaApi.getProcessInstances();
      setInstances(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load process instances');
    } finally {
      setLoading(false);
    }
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
                <th>Process Definition ID</th>
                <th>Business Key</th>
                <th>Variables</th>
              </tr>
            </thead>
            <tbody>
              {instances.map((instance) => (
                <tr key={instance.id}>
                  <td>{instance.id}</td>
                  <td>{instance.processDefinitionId}</td>
                  <td>{instance.businessKey || 'N/A'}</td>
                  <td>
                    <details>
                      <summary>View Variables</summary>
                      <pre style={{ marginTop: '0.5rem', fontSize: '0.85rem' }}>
                        {JSON.stringify(instance.variables, null, 2)}
                      </pre>
                    </details>
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



