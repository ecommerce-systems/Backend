package com.creepereye.ecommerce.domain.user.entity;

import com.creepereye.ecommerce.domain.auth.entity.Auth;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String phone;

    private String address;

    private Boolean active;

    private String clubMemberStatus;

    private String fashionNewsFrequency;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_id", nullable = false, unique = true)
    private Auth auth;
}