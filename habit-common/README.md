# Habit Common

Shared library containing common code and Protobuf definitions for the Habit Tracking System.

## Contents

### Protobuf Definitions

```protobuf
// user.proto
service UserService {
  rpc GetUser (GetUserRequest) returns (UserResponse) {}
  rpc GetUserByUsername (GetUserByUsernameRequest) returns (UserResponse) {}
  rpc CreateUser (CreateUserRequest) returns (UserResponse) {}
  rpc UpdateUser (UpdateUserRequest) returns (UserResponse) {}
}

// auth.proto
service AuthService {
  rpc Login (LoginRequest) returns (LoginResponse) {}
  rpc ValidateToken (ValidateTokenRequest) returns (ValidateTokenResponse) {}
}
```

## Usage

Add as dependency in service `pom.xml`:

```xml
<dependency>
    <groupId>com.habitsystem</groupId>
    <artifactId>habit-common</artifactId>
    <version>${project.version}</version>
</dependency>
```

## Building

```bash
mvn clean install
```

## Dependencies

- Protocol Buffers
- gRPC
- Spring Boot