# Kafka Daily Commands Guide

This guide contains commonly used Kafka commands for daily operations in the Commercial Site project.

## Prerequisites

- Kafka is running via Docker Compose
- Kafka broker: `localhost:9092`
- Container name: `commercial_site-kafka-1`

---

## Container Management

### Start Kafka and Zookeeper
```bash
docker-compose up -d zookeeper kafka
```

### Stop Kafka and Zookeeper
```bash
docker-compose stop kafka zookeeper
```

### Restart Kafka
```bash
docker-compose restart kafka
```

### View Kafka Logs
```bash
docker logs commercial_site-kafka-1
```

### View Kafka Logs (Follow Mode)
```bash
docker logs -f commercial_site-kafka-1
```

### Check Container Status
```bash
docker ps | grep kafka
```

---

## Topic Management

### List All Topics
```bash
docker exec commercial_site-kafka-1 kafka-topics --bootstrap-server localhost:9092 --list
```

### Create a Topic
```bash
docker exec commercial_site-kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --topic <topic-name> --partitions 1 --replication-factor 1
```

### Create Project Topic (orders.created)
```bash
docker exec commercial_site-kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic orders.created --partitions 1 --replication-factor 1
```

### Describe a Topic
```bash
docker exec commercial_site-kafka-1 kafka-topics --bootstrap-server localhost:9092 --describe --topic <topic-name>
```

### Describe Project Topic
```bash
docker exec commercial_site-kafka-1 kafka-topics --bootstrap-server localhost:9092 --describe --topic orders.created
```

### Delete a Topic
```bash
docker exec commercial_site-kafka-1 kafka-topics --bootstrap-server localhost:9092 --delete --topic <topic-name>
```

### List Topic Partitions
```bash
docker exec commercial_site-kafka-1 kafka-topics --bootstrap-server localhost:9092 --describe --topic <topic-name> | grep Partition
```

---

## Producing Messages

### Produce Messages (Interactive)
```bash
docker exec -it commercial_site-kafka-1 kafka-console-producer --bootstrap-server localhost:9092 --topic <topic-name>
```

### Produce to Project Topic
```bash
docker exec -it commercial_site-kafka-1 kafka-console-producer --bootstrap-server localhost:9092 --topic orders.created
```

### Produce JSON Message (Example)
```bash
docker exec -i commercial_site-kafka-1 kafka-console-producer --bootstrap-server localhost:9092 --topic orders.created <<EOF
{"orderId": "123", "customerId": "456", "status": "CREATED"}
EOF
```

### Produce from File
```bash
docker exec -i commercial_site-kafka-1 kafka-console-producer --bootstrap-server localhost:9092 --topic <topic-name> < <file-path>
```

---

## Consuming Messages

### Consume Messages (From Beginning)
```bash
docker exec -it commercial_site-kafka-1 kafka-console-consumer --bootstrap-server localhost:9092 --topic <topic-name> --from-beginning
```

### Consume from Project Topic
```bash
docker exec -it commercial_site-kafka-1 kafka-console-consumer --bootstrap-server localhost:9092 --topic orders.created --from-beginning
```

### Consume Latest Messages Only
```bash
docker exec -it commercial_site-kafka-1 kafka-console-consumer --bootstrap-server localhost:9092 --topic <topic-name>
```

### Consume with Consumer Group
```bash
docker exec -it commercial_site-kafka-1 kafka-console-consumer --bootstrap-server localhost:9092 --topic <topic-name> --group <group-name>
```

### Consume with Project Consumer Groups
```bash
# Notification Service Group
docker exec -it commercial_site-kafka-1 kafka-console-consumer --bootstrap-server localhost:9092 --topic orders.created --group notification-service

# Camunda Service Group
docker exec -it commercial_site-kafka-1 kafka-console-consumer --bootstrap-server localhost:9092 --topic orders.created --group camunda-service
```

### Consume with Pretty Print (JSON)
```bash
docker exec -it commercial_site-kafka-1 kafka-console-consumer --bootstrap-server localhost:9092 --topic <topic-name> --from-beginning --property print.key=true --property print.value=true
```

---

## Consumer Group Management

### List All Consumer Groups
```bash
docker exec commercial_site-kafka-1 kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

### Describe Consumer Group
```bash
docker exec commercial_site-kafka-1 kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group <group-name>
```

### Describe Project Consumer Groups
```bash
# Notification Service
docker exec commercial_site-kafka-1 kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group notification-service

# Camunda Service
docker exec commercial_site-kafka-1 kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group camunda-service
```

### Reset Consumer Group Offset (To Beginning)
```bash
docker exec commercial_site-kafka-1 kafka-consumer-groups --bootstrap-server localhost:9092 --group <group-name> --reset-offsets --to-earliest --topic <topic-name> --execute
```

### Reset Consumer Group Offset (To Latest)
```bash
docker exec commercial_site-kafka-1 kafka-consumer-groups --bootstrap-server localhost:9092 --group <group-name> --reset-offsets --to-latest --topic <topic-name> --execute
```

### Delete Consumer Group
```bash
docker exec commercial_site-kafka-1 kafka-consumer-groups --bootstrap-server localhost:9092 --delete --group <group-name>
```

---

## Monitoring & Debugging

### Get Broker Information
```bash
docker exec commercial_site-kafka-1 kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Check Topic Message Count (Approximate)
```bash
docker exec commercial_site-kafka-1 kafka-run-class kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic <topic-name> --time -1
```

### View Topic Offsets
```bash
docker exec commercial_site-kafka-1 kafka-run-class kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic <topic-name>
```

### Check Consumer Lag
```bash
docker exec commercial_site-kafka-1 kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group <group-name> | grep -E "TOPIC|LAG"
```

---

## Quick Reference: Project-Specific Commands

### Start All Services (Including Kafka)
```bash
docker-compose up -d
```

### Create Project Topic
```bash
docker exec commercial_site-kafka-1 kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic orders.created --partitions 1 --replication-factor 1
```

### Monitor Project Topic
```bash
docker exec -it commercial_site-kafka-1 kafka-console-consumer --bootstrap-server localhost:9092 --topic orders.created --from-beginning
```

### Check Consumer Groups Status
```bash
docker exec commercial_site-kafka-1 kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

### View Notification Service Consumer Group
```bash
docker exec commercial_site-kafka-1 kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group notification-service
```

### View Camunda Service Consumer Group
```bash
docker exec commercial_site-kafka-1 kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group camunda-service
```

---

## Troubleshooting

### Kafka Not Starting
```bash
# Check logs
docker logs commercial_site-kafka-1

# Check if Zookeeper is running
docker ps | grep zookeeper

# Restart both services
docker-compose restart zookeeper kafka
```

### Connection Refused Errors
```bash
# Verify Kafka is listening on port 9092
docker exec commercial_site-kafka-1 netstat -tuln | grep 9092

# Check container health
docker ps | grep kafka
```

### Clear All Topics (⚠️ Use with Caution)
```bash
# List all topics first
docker exec commercial_site-kafka-1 kafka-topics --bootstrap-server localhost:9092 --list

# Delete specific topic
docker exec commercial_site-kafka-1 kafka-topics --bootstrap-server localhost:9092 --delete --topic <topic-name>
```

### Reset Consumer Group to Replay Messages
```bash
# Stop the consumer application first, then:
docker exec commercial_site-kafka-1 kafka-consumer-groups --bootstrap-server localhost:9092 --group <group-name> --reset-offsets --to-earliest --topic orders.created --execute
```

---

## Tips

1. **Always use `--bootstrap-server`** instead of `--zookeeper` (deprecated in newer Kafka versions)
2. **Use `--from-beginning`** when consuming to see all messages
3. **Consumer groups** allow multiple consumers to share the load
4. **Check consumer lag** regularly to ensure messages are being processed
5. **Use `docker exec -it`** for interactive commands that require input
6. **Use `docker exec -i`** when piping input from files or heredoc

---

## Project Topics

| Topic Name | Description | Consumer Groups |
|------------|-------------|-----------------|
| `orders.created` | Order creation events | `notification-service`, `camunda-service` |

---

## Additional Resources

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Confluent Platform Documentation](https://docs.confluent.io/)
- Docker Compose file: `docker-compose.yml`







