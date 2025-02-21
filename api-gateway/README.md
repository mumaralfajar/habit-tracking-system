# API Gateway

API Gateway service for the Habit Tracking System. Routes and manages all client requests.

## Features

- Request routing
- Authentication middleware
- Rate limiting
- CORS support
- Error handling
- gRPC-HTTP transcoding

## Routes

```yaml
/api/auth/**  -> auth-service:8091 (HTTP)
/api/users/** -> user-service:9092 (gRPC)
/api/habits/**-> habit-service:9093 (gRPC)
```

## Configuration

### Environment Variables

```properties
SERVER_PORT=8080
AUTH_SERVICE_HOST=auth-service
AUTH_SERVICE_PORT=8091
USER_SERVICE_HOST=user-service
USER_SERVICE_PORT=9092
```

## Security

- Token validation via Auth Service
- Rate limiting
- CORS configuration
- Request validation

## Building and Running

### Local Development

```bash
mvn spring-boot:run
```

### Docker

```bash
docker build -t api-gateway .
docker run -p 8080:8080 api-gateway
```

## Dependencies

- Spring Cloud Gateway
- Spring WebFlux
- gRPC