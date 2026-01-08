package com.creepereye.ecommerce.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Long expiryDate; // Using timestamp or LocalDateTime. Redis uses seconds/millis. Let's use Long for consistency or LocalDateTime? The current Redis impl passes `validityInSeconds`. Let's use expiration timestamp (millis).
}
