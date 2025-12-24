package com.creepereye.ecommerce.domain.user.repository;


import com.creepereye.ecommerce.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
    @Query("SELECT u FROM User u WHERE u.auth.username = :username")
    Optional<User> findByAuthUsername(@Param("username") String username);
}
