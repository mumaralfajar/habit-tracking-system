# Habit Tracking System

A microservices-based habit tracking system built with Spring Boot and gRPC.

## Architecture

[comment]: # ("image is based on percy/C4_Container Diagram Sample - bigbankplc-styles.puml")
![Container diagram for Habit Tracking System](https://www.plantuml.com/plantuml/png/hLTHR-9647xthvX35nMa95IdFbKwwHBWxd8b2GLozsYjxG5MMNRTtJ49glhVExlU0CQKzg5lx2pppVVDZ3_C9qV2KhLMCFWb_IeoIHSVVfvRaJzBlUIFiwqZNATZtA3YuuVadLp0nKPOAJA5ARvAHwxRcOx6D_ExxwFqxc4mVXgEEhq4u9tKkQeAX8ibKhhBsGXLuQcKFbCO64r2QhJN9PDAtg5oU0Yr8YhTh_s-5I_NIqchAgiSsfopKDDrRjRzKWdj1LqvAfJC6FHguaDFz-F-MZX2UxAEBkGYIMPV7t-aaz5qzlYGFZxDntSFesulIMwAOefgBfRTZd1RdNSkuJtXAmsCClRZ-vlfbDC-FpxCqy7Z-77Ajugn6xU75pFQAlmu56w5nPZFtLxli8IlqFyVwnYYVPs492c4dNuOIh6qOWqBOo4i0BoD7-hX9ya4hJEwwpjz2PqVcC5DMS8J7pjyljE4d44ENdWWG2i45nA13937WfN7THsk7lVfrbIw47RR3JTf_Nt6EacWquC_4m3Oa-w-O9QAijopKJ8N98tsX6xqibA2gSwt9SvoAqlouOar6rcW2moz5f2U-KBaoCoQ-AAKwL8GleYj1vpSmPVwv85cfVNa1ifKHIFEKOM0ckmMIiFeeQb2ASYLH3vQ_AD2H-wQEwqBXGwigOZJBa5KJ4jJb749bd51oRMCrxe0PKG1cL12vnnhSsQ4b6LjP8wUD1zXLXyFMDyQGs5gpZ6LWBiHIXQ2-FJjnnp8FADsBNJVi0DqF_mpwFT2YoMwkisbDGkfOh7IuW8jwXpRDU8on29n6yvK6HYB49A0tbPcKUdSTv0rqRPLGHkIYzZZWq8F1-7_gkeGgYt5OcdiePPf29m1_uAS5wOWcFoMPEukVUkM5fs36i_LYo3obSGDhb7lXHyp66RrGXJPsouCRyDJO1mnvEptSU10Xk6FbWyAGU84dfVDU77wv_12WqwZn24pJ9plEPoZsPv6ZBtsaB7DPp5Z9riJMwDpFDqqi-Ov-DnlOl4imjZh6wXlmZCTt1boirvNEdQD0V_Q-TlebRgu5b8TRC38dniBS3-VWB-Fe0vruMM6nDP-9J_vnpC-h65C2sLUN395bOwRXdNa15DJKHpope727dq_VF3h4amShHQggGsivwGTJ4UpkQyOVmkY3TSRuRoch_FvPCPG_ZkxswQRtejdT3iYkwG-OqSIqLE134GLVgLyQvRJoI0edZOLUpfjnJwwBzGxbHWsh9qPmkTJuoArSqEyaSoIXj9YJczohiXSnO_DHX_9RDd8CTtFIFbgnpSy7nprwU7LGIpTnyFNYQYZ1HlQrqRruNy5ZVe4YKmuPDcgNjcLB5tGqDp6Q4fDxzaL-JQy7SJjQNAAtjEoc5RQKOTsRjFqb7PckzSxMpdoZbOooxy9Ve0Rr7Jbsr0qntgY3osxcLIPacuLEr9ZkNt3Ow_RJCzZHSQdqGBrfz9RGCDkJWpnkCG__93EgipbLcPScSmv3MnPfo6ENNE6kd17PXYzav7g5_RnwCleOSWlwWjZoA-55I-wVXjzU_M7pJLSRD0w1kZsWll5FmRnbe_VtwwJJrpK_otu6m00)

### Service Responsibilities

- **API Gateway**: Routes and manages client requests (port 8080)
- **Auth Service**: Handles authentication and authorization (ports 8091/9091)
- **User Service**: Manages user profiles and data (ports 8092/9092)
- **Notification Service**: Handles email and push notifications (ports 8093/9093)
- **Habit Service**: Core habit tracking functionality (future implementation)

## Technologies

- Java 17
- Spring Boot 3.x
- gRPC
- Kafka
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

| Service             | HTTP Port | gRPC Port |
|---------------------|-----------|-----------|
| API Gateway         | 8080      | -         |
| Auth Service        | 8091      | 9091      |
| User Service        | 8092      | 9092      |
| Notification Service| 8093      | 9093      |
| PostgreSQL          | 5432      | -         |

## Project Structure

```
habit-tracking-system/
├── api-gateway/          # API Gateway service
├── auth-service/         # Authentication service
├── habit-common/         # Shared code and protobuf definitions
├── user-service/         # User management service
├── notification-service/ # Notification handling service
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