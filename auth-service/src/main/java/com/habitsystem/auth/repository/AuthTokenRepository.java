package com.habitsystem.auth.repository;

import com.habitsystem.auth.model.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {
    Optional<AuthToken> findByRefreshToken(String refreshToken);
    void deleteByUserId(UUID userId);
}
