package com.habitsystem.user.mapper;

import com.habitsystem.user.model.User;
import com.habitsystem.proto.user.UserResponse;
import com.habitsystem.proto.user.CreateUserRequest;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public User toEntity(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        return user;
    }
    
    public UserResponse toProto(User user) {
        return UserResponse.newBuilder()
            .setUserId(user.getId().toString())
            .setUsername(user.getUsername())
            .setEmail(user.getEmail())
            .build();
    }
}
