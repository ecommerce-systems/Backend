package com.creepereye.ecommerce.domain.user.service;

import com.creepereye.ecommerce.domain.auth.dto.SignUpRequest;
import com.creepereye.ecommerce.domain.auth.entity.Auth;
import com.creepereye.ecommerce.domain.auth.repository.AuthRepository;
import com.creepereye.ecommerce.domain.user.dto.UserResponse;
import com.creepereye.ecommerce.domain.user.dto.UserUpdateRequest;
import com.creepereye.ecommerce.domain.user.entity.User;
import com.creepereye.ecommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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

    @Transactional
    public void signUpAdmin(SignUpRequest signUpRequest) {
        if (authRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        Auth auth = Auth.builder()
                .username(signUpRequest.getUsername())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .roles(Arrays.asList("USER", "ADMIN"))
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

    @Transactional(readOnly = true)
    public UserResponse getUserInfo() {
        User user = getCurrentAuthenticatedUser();
        return new UserResponse(user);
    }

    @Transactional
    public void updateUserInfo(UserUpdateRequest updateRequest) {
        User user = getCurrentAuthenticatedUser();
        user.setPhone(updateRequest.getPhone());
        user.setAddress(updateRequest.getAddress());
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount() {
        User user = getCurrentAuthenticatedUser();
        Auth auth = user.getAuth();

        userRepository.delete(user);
        authRepository.delete(auth);
    }

    private User getCurrentAuthenticatedUser() {
        String username;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findByAuthUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
