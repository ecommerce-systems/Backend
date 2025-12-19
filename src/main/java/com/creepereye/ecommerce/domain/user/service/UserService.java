package com.creepereye.ecommerce.domain.user.service;

import com.creepereye.ecommerce.domain.auth.dto.SignUpRequest;
import com.creepereye.ecommerce.domain.auth.entity.Auth;
import com.creepereye.ecommerce.domain.auth.repository.AuthRepository;
import com.creepereye.ecommerce.domain.user.entity.User;
import com.creepereye.ecommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(SignUpRequest signUpRequest) {
        if (authRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        Auth auth = Auth.builder()
                .username(signUpRequest.getUsername())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .roles(Collections.singletonList("USER"))
                .build();
        authRepository.save(auth);

        User user = User.builder()
                .name(signUpRequest.getName())
                .phone(signUpRequest.getPhone())
                .address(signUpRequest.getAddress())
                .auth(auth)
                .build();
        userRepository.save(user);
    }
}
