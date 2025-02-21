# Habit Tracking System

A microservices-based habit tracking system built with Spring Boot and gRPC.

## Architecture

```
                    ┌─────────────────┐
                    │   API Gateway   │
                    │  (Spring Cloud) │
                    └────────┬────────┘
                            │
              ┌────────────┼────────────┐
              │            │            │
    ┌─────────┴─────┐ ┌───┴────┐ ┌────┴─────┐
    │  User Service │ │  Auth  │ │  Habit   │
    │  (gRPC/HTTP)  │ │Service │ │ Service  │
    └───────┬───────┘ └───┬────┘ └────┬─────┘
            │             │           │
    ┌───────┴───────┐ ┌──┴───┐ ┌────┴─────┐
    │ User Database │ │ Auth │ │  Habit   │
    │  (Postgres)   │ │  DB  │ │    DB    │
    └───────────────┘ └──────┘ └──────────┘
```

## Services

- **API Gateway**: Routes and manages client requests (port 8080)
- **Auth Service**: Handles authentication and authorization (ports 8091/9091)
- **User Service**: Manages user profiles and data (ports 8092/9092)
- **Habit Service**: Core habit tracking functionality (future implementation)

## Technologies

- Java 17
- Spring Boot 3.x
- gRPC
- PostgreSQL
- Docker
- Maven

## Getting Started

1. Clone the repository:
```bash
git clone https://github.com/yourusername/habit-tracking-system.git
```

2. Build all services:
```bash
mvn clean install
```

3. Start the system:
```bash
docker-compose up --build
```

## Service Ports

| Service      | HTTP Port | gRPC Port |
|--------------|-----------|-----------|
| API Gateway  | 8080      | -         |
| Auth Service | 8091      | 9091      |
| User Service | 8092      | 9092      |
| PostgreSQL   | 5432      | -         |

## Project Structure

```
habit-tracking-system/
├── api-gateway/          # API Gateway service
├── auth-service/         # Authentication service
├── habit-common/         # Shared code and protobuf definitions
├── user-service/         # User management service
└── docker-compose.yml    # Docker composition file
```

## API Documentation

### Auth Service Endpoints

```
POST /api/auth/register
POST /api/auth/login
```

### User Service Endpoints (via gRPC)

```
GetUser
GetUserByUsername
CreateUser
UpdateUser
```

## Development

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker
- PostgreSQL (for local development)

### Building

```bash
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Running Locally

1. Start PostgreSQL:
```bash
docker-compose up postgres
```

2. Run each service:
```bash
mvn spring-boot:run
```

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.