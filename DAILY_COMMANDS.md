# Daily Commands Reference

Quick reference for common development commands.

## Windows Port Management

### Check Which Ports Are In Use

#### Using Command Prompt (CMD)
```cmd
# List all listening ports with process IDs
netstat -ano

# Find a specific port (e.g., port 5050)
netstat -ano | findstr :5050

# List only listening ports
netstat -ano | findstr LISTENING

# List ports with process names (requires admin)
netstat -ano | findstr LISTENING
```

#### Using PowerShell (Recommended)
```powershell
# List all listening ports with process info
Get-NetTCPConnection | Where-Object {$_.State -eq "Listen"} | Select-Object LocalPort, OwningProcess | Format-Table

# Find a specific port (e.g., port 5050)
Get-NetTCPConnection -LocalPort 5050 -ErrorAction SilentlyContinue | Select-Object LocalPort, State, OwningProcess

# Get process name for a port
$port = 5050
$process = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
Get-Process -Id $process | Select-Object Id, ProcessName, Path
```

### Close/Kill Process on a Port

#### Method 1: Using netstat + taskkill (CMD)
```cmd
# Step 1: Find the PID for the port
netstat -ano | findstr :5050

# Step 2: Kill the process (replace <PID> with actual process ID)
taskkill /PID <PID> /F

# Example: If PID is 12345
taskkill /PID 12345 /F
```

#### Method 2: One-liner (CMD)
```cmd
# Find and kill process on port 5050
for /f "tokens=5" %a in ('netstat -ano ^| findstr :5050') do taskkill /PID %a /F
```

#### Method 3: Using PowerShell (Recommended)
```powershell
# Find and kill process on port 5050
$port = 5050
$process = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
if ($process) {
    Stop-Process -Id $process -Force
    Write-Host "Process on port $port has been killed"
} else {
    Write-Host "No process found on port $port"
}

# One-liner version
Get-NetTCPConnection -LocalPort 5050 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```

#### Method 4: PowerShell Function (Add to Profile)
```powershell
# Add this function to your PowerShell profile for easy reuse
function Kill-Port {
    param([int]$Port)
    $process = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
    if ($process) {
        Stop-Process -Id $process -Force
        Write-Host "Process on port $Port has been killed"
    } else {
        Write-Host "No process found on port $Port"
    }
}

# Usage: Kill-Port 5050
```

### Common Ports in This Project

| Service | Port | Command to Kill |
|---------|------|-----------------|
| Eureka Server | 8761 | `Get-NetTCPConnection -LocalPort 8761 -ErrorAction SilentlyContinue \| ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }` |
| Config Server | 8888 | `Get-NetTCPConnection -LocalPort 8888 -ErrorAction SilentlyContinue \| ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }` |
| Gateway | 8080 | `Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue \| ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }` |
| Camunda Service | 5050 | `Get-NetTCPConnection -LocalPort 5050 -ErrorAction SilentlyContinue \| ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }` |
| Order Service | 6060 | `Get-NetTCPConnection -LocalPort 6060 -ErrorAction SilentlyContinue \| ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }` |
| Notification Service | 7075 | `Get-NetTCPConnection -LocalPort 7075 -ErrorAction SilentlyContinue \| ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }` |
| Kafka | 9092 | `Get-NetTCPConnection -LocalPort 9092 -ErrorAction SilentlyContinue \| ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }` |
| React Frontend | 3000 | `Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue \| ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }` |
| MailHog Web UI | 8025 | `Get-NetTCPConnection -LocalPort 8025 -ErrorAction SilentlyContinue \| ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }` |
| MailHog SMTP | 1025 | `Get-NetTCPConnection -LocalPort 1025 -ErrorAction SilentlyContinue \| ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }` |

## Docker Commands

### Docker Compose
```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose stop

# Stop and remove containers
docker-compose down

# View logs
docker-compose logs -f kafka
docker-compose logs -f mailhog

# Check service status
docker-compose ps
```

## Maven Commands

### Build and Run Services
```bash
# Build project
mvn clean install

# Run specific service
cd camunda-service
mvn spring-boot:run

# Skip tests
mvn clean install -DskipTests
```

## Node.js Commands

### React Frontend
```bash
# Install dependencies
cd camunda-frontend
npm install

# Start development server
npm start

# Build for production
npm run build
```

## MySQL Commands

### Database Operations
```bash
# Connect to MySQL
mysql -u springstudent -p springstudent

# Use camundadb
USE camundadb;

# Show tables
SHOW TABLES;

# Run SQL script
SOURCE camunda-service/src/main/resources/db/insert-sample-users-groups-simple.sql;
```

## Kill All Java Processes

### Using Command Prompt (CMD)
```cmd
# Kill all Java processes
taskkill /F /IM java.exe
taskkill /F /IM javaw.exe

# Kill all Java processes (both java.exe and javaw.exe)
taskkill /F /IM java.exe /IM javaw.exe
```

### Using PowerShell (Recommended)
```powershell
# Kill all Java processes
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Get-Process javaw -ErrorAction SilentlyContinue | Stop-Process -Force

# One-liner to kill all Java processes
Get-Process | Where-Object {$_.ProcessName -like "*java*"} | Stop-Process -Force

# More specific - only java.exe and javaw.exe
Get-Process java,javaw -ErrorAction SilentlyContinue | Stop-Process -Force
```

### Check Java Processes and Their Ports
```powershell
# List all Java processes with their ports
Get-Process | Where-Object {$_.ProcessName -like "*java*"} | ForEach-Object {
    $process = $_
    $ports = Get-NetTCPConnection -OwningProcess $process.Id -ErrorAction SilentlyContinue | Select-Object -ExpandProperty LocalPort -Unique
    [PSCustomObject]@{
        PID = $process.Id
        ProcessName = $process.ProcessName
        Ports = ($ports | Sort-Object) -join ', '
        Path = $process.Path
    }
} | Format-Table -AutoSize

# Count Java processes
(Get-Process | Where-Object {$_.ProcessName -like "*java*"}).Count
```

### Show All Ports Used by Java Processes
```powershell
# Get all ports used by Java processes
$javaProcesses = Get-Process | Where-Object {$_.ProcessName -like "*java*"}
$allPorts = @()
foreach ($proc in $javaProcesses) {
    $ports = Get-NetTCPConnection -OwningProcess $proc.Id -ErrorAction SilentlyContinue | Select-Object -ExpandProperty LocalPort -Unique
    $allPorts += $ports
}
$allPorts | Sort-Object -Unique | ForEach-Object { Write-Host "Port: $_" }
```

### Kill All Java Processes on All Ports
```powershell
# Kill all Java processes (this will free all ports they're using)
Get-Process java,javaw -ErrorAction SilentlyContinue | Stop-Process -Force

# Verify all Java processes are killed
$remaining = Get-Process | Where-Object {$_.ProcessName -like "*java*"}
if ($remaining) {
    Write-Host "Warning: Some Java processes still running:" -ForegroundColor Yellow
    $remaining | Select-Object Id, ProcessName
} else {
    Write-Host "All Java processes killed successfully" -ForegroundColor Green
}
```

### Kill All Java Processes and Show Freed Ports
```powershell
# Get ports before killing
$javaProcesses = Get-Process | Where-Object {$_.ProcessName -like "*java*"}
$portsBefore = @()
foreach ($proc in $javaProcesses) {
    $ports = Get-NetTCPConnection -OwningProcess $proc.Id -ErrorAction SilentlyContinue | Select-Object -ExpandProperty LocalPort -Unique
    $portsBefore += $ports
}
$portsBefore = $portsBefore | Sort-Object -Unique

# Kill all Java processes
Get-Process java,javaw -ErrorAction SilentlyContinue | Stop-Process -Force

# Show freed ports
Write-Host "Freed ports:" -ForegroundColor Green
$portsBefore | ForEach-Object { Write-Host "  Port $_" }
```

### Kill Java Processes by Port (Alternative)
```powershell
# If you know Java is using specific ports, kill by port instead
$ports = @(8761, 8888, 8080, 5050, 6060, 7075, 9092, 3000, 8025, 1025)
foreach ($port in $ports) {
    $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($conn) {
        $process = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
        if ($process.ProcessName -like "*java*") {
            Stop-Process -Id $conn.OwningProcess -Force
            Write-Host "Killed Java process on port $port"
        }
    }
}
```

## Quick Troubleshooting

### Port Already in Use
```powershell
# Find what's using the port
Get-NetTCPConnection -LocalPort 5050

# Kill it
Get-NetTCPConnection -LocalPort 5050 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```

### Check All Project Ports at Once
```powershell
# PowerShell script to check all project ports
$ports = @(8761, 8888, 8080, 5050, 6060, 7075, 9092, 3000, 8025, 1025)
foreach ($port in $ports) {
    $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($conn) {
        $process = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
        Write-Host "Port $port : $($process.ProcessName) (PID: $($conn.OwningProcess))" -ForegroundColor Green
    } else {
        Write-Host "Port $port : Available" -ForegroundColor Gray
    }
}
```

### Kill All Project Services
```powershell
# Kill all services on project ports
$ports = @(8761, 8888, 8080, 5050, 6060, 7075, 9092, 3000, 8025, 1025)
foreach ($port in $ports) {
    Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | ForEach-Object { 
        Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue
    }
}
Write-Host "All project services killed"
```

## Notes

- **PowerShell** commands are recommended as they provide better process information
- Some commands may require **Administrator privileges**
- Always check what process is using a port before killing it
- Use `-ErrorAction SilentlyContinue` to avoid errors when port is not in use

