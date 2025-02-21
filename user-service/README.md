# User Service

User management microservice for the Habit Tracking System. Handles user CRUD operations via gRPC.

## Features

- User creation and management
- gRPC API endpoints
- PostgreSQL integration
- Health monitoring

## API

### gRPC Endpoints

```protobuf
service UserService {
  rpc GetUser (GetUserRequest) returns (UserResponse) {}
  rpc GetUserByUsername (GetUserByUsernameRequest) returns (UserResponse) {}
  rpc CreateUser (CreateUserRequest) returns (UserResponse) {}
  rpc UpdateUser (UpdateUserRequest) returns (UserResponse) {}
}
```

### Health Checks

- Endpoint: `/actuator/health`
- Includes: database connectivity, gRPC server status

## Configuration

### Environment Variables

```properties
SERVER_PORT=8092
GRPC_SERVER_PORT=9092
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/habitsystem
SPRING_DATASOURCE_USERNAME=habitsystem
SPRING_DATASOURCE_PASSWORD=habitsystem
```

### Database Schema

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## Building and Running

### Local Development

```bash
mvn spring-boot:run
```

### Docker

```bash
docker build -t user-service .
docker run -p 8092:8092 -p 9092:9092 user-service
```

## Testing

```bash
mvn test
```

## Dependencies

- Spring Boot
- gRPC
- PostgreSQL
- Flyway
- Spring Security