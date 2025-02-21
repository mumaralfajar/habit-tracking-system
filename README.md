# Habit Tracking System

A microservices-based habit tracking system built with Spring Boot and gRPC.

## Architecture

[comment]: # ("image is based on percy/C4_Container Diagram Sample - bigbankplc-styles.puml")
![Container diagram for Habit Tracking System](https://www.plantuml.com/plantuml/png/hLTFRzku4xthKqoD5ZyRIEBBxxH0WIQsswPm4bTssgD0IMERA4LgoP5TOx7VVKcHKVJ7Q-Imjt2OUVFUpEX9_cI8QQeA4RtZM_XmO9gpL622lxaXCnx5Y-Njvlx78hb_d2sVvulH909unsKcgXpXmmufQN8EJ5HuBcKw-tyoKv8ObwXloheQ2eDjg3rHQVwOJZKxtkmuxQkqCgWpcuMIRZ9LJ4l1f6DwRIWNFBMWroiNUdvOJWjc2FNPEZBdsoXQVttwcQmMyVhfCNbwtYpl7nVZIHJTvdcCOiDsun4p9vcDhk0zuM-QAQ7qn_UtSMpJFZyzRfBPq_8fjhV2OhTlEnThEWdyE6TcZ_dIdiUJIRk4gp3z3-ieejSw4N4I28r-c7Esqwo0hTBmbQMSOADPzelB7QnFXh28eXLge-JOTVeAHZynXTkoX6Tx7DdplIIq6MJWQ0S2j4SmTIAG0d9Oi7UuPcIhUyZaJbKoP_eqhcyI__yMwoo1qGJ-YW3WbVJuY6d2olALZU0P8wwa8nIZPNC5crE9wqppabnmfTM1vsXgVau9SCTxopAqlBhehEJ9ZX4UsSbctgxkuOi_EQ1rgHsrcL1LtedRg410IVe4fRBeTKkP4909ZlQeySyA3PaRssUP2pIWLKKsxGfOPMb92YAkG5jS4BpWuLhc81JB8MM2oSp6XfmjGc9b7NY6ZhGzmjeVMwplbA9wPiPOAZNkWGcUCxAdRpytGEeNIZD0TmrheRlHNq1_O9BjqFWsbrfjkGZ5IerRr2Wp7DO8gn2AX5suK6McDFhD0dfPc6qbCzT1gubEWmfI4T-67hSAFRR2RrNLXXfAqLWgtTOIky0bCL_GvZ6gsyJ4YNXchbphTXgD0Ozd_2AmRC_nW0NAL-5z1lFKBqIUlkp2_CwLMIb35dBzVLbp86NXUyi7EIDs1i_9jdXX-XVmV8FEeuGXMvWmtqiulTcUHmozTf2XpHSnGoS74olG6BR39DNs0T0EYgMs-iFtUlB-4lobVwoTFKEcYgAIeN4My-_6u1Q_QOm5uwAr10jtxkp0msO5xZw06fIvKred3bOi-fzxGiFpweAm5UfeeXX5iknwLiyCObLHc7DZGlKg_MW_-uxHBbxDV2ah0BLaegiHjECc3SIBzSPL3Y-5uCT-EOn3-hhPhDOMolrFSzirrWVs2qr3g4cQEcnF8jWha8AWncsNQv4ZMYkFkyeTdQ5o5-qK2Zg707xf1WW-_3Q8i6C3Z10VW7YfSwunenUPrwIkmv-z0NJbzvncIFypKhPl-Bj7g2dhlYtOpdniVsy4FGDOtzO1gWk_1TJfOyHIPX0q2h_IUrwQMaDtMuDlTUsfAV9jVXUwtdid1NiQkD0WhTUXnf1wjZFC73IbSPwUlGoIhVpRsY_mW9AkNHloxbZFz67WIAig5TpiGqSybdbjUEZraEb5hC3uF9gd_bmwY-ZOqPaXzalyoxjsNQKcqporbKbTqc0jxJn4trNNA7FJCylWhHR9Vz4l5ryMZ_Fn9DegHMujD3jA_xdwyji03ZVmuO3QM83nf7R7yCiXtDhZZvVhw9CjMXNY7m00)

### Service Responsibilities

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