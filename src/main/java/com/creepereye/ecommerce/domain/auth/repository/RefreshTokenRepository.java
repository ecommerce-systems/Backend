package com.creepereye.ecommerce.domain.auth.repository;

import com.creepereye.ecommerce.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUsername(String username);
    void deleteByUsername(String username);
}
