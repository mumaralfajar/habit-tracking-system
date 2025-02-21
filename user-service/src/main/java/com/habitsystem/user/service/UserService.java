package com.habitsystem.user.service;

import com.habitsystem.user.model.User;
import java.util.Optional;

public interface UserService {
    User createUser(User user);
    Optional<User> getUserById(String id);
    Optional<User> getUserByUsername(String username);
    User updateUser(User user);
    void deleteUser(String id);
}
