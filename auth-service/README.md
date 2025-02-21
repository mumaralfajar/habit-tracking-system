# Auth Service

Authentication and authorization microservice for the Habit Tracking System.

## Features

- User registration
- Login with JWT tokens
- Token validation
- Integration with User Service

## API

### REST Endpoints

```
POST /api/auth/register
Body: {
    "username": string,
    "email": string,
    "password": string
}

POST /api/auth/login
Body: {
    "username": string,
    "password": string
}
```

### gRPC Endpoints

```protobuf
service AuthService {
  rpc ValidateToken (ValidateTokenRequest) returns (ValidateTokenResponse) {}
}
```

## Configuration

### Environment Variables

```properties
SERVER_PORT=8091
GRPC_SERVER_PORT=9091
JWT_SECRET=your-256-bit-secret
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
```

## Security

- JWT token-based authentication
- BCrypt password hashing
- Token refresh mechanism

## Building and Running

### Local Development

```bash
mvn spring-boot:run
```

### Docker

```bash
docker build -t auth-service .
docker run -p 8091:8091 -p 9091:9091 auth-service
```

## Dependencies

- Spring Boot
- gRPC
- JWT
- Spring Security