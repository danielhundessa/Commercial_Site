import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import TaskList from './components/TaskList';
import TaskDetail from './components/TaskDetail';
import ProcessInstances from './components/ProcessInstances';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <nav className="navbar">
          <div className="nav-container">
            <h1 className="nav-logo">Camunda Task Manager</h1>
            <ul className="nav-menu">
              <li className="nav-item">
                <Link to="/" className="nav-link">Tasks</Link>
              </li>
              <li className="nav-item">
                <Link to="/process-instances" className="nav-link">Process Instances</Link>
              </li>
            </ul>
          </div>
        </nav>
        
        <main className="main-content">
          <Routes>
            <Route path="/" element={<TaskList />} />
            <Route path="/task/:taskId" element={<TaskDetail />} />
            <Route path="/process-instances" element={<ProcessInstances />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;






