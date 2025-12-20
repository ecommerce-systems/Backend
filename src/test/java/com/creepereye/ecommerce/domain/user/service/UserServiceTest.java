package com.creepereye.ecommerce.domain.user.service;

import com.creepereye.ecommerce.domain.auth.dto.SignUpRequest;
import com.creepereye.ecommerce.domain.auth.entity.Auth;
import com.creepereye.ecommerce.domain.auth.repository.AuthRepository;
import com.creepereye.ecommerce.domain.user.entity.User;
import com.creepereye.ecommerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private SignUpRequest signUpRequest;

    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("testuser");
        signUpRequest.setPassword("password");
        signUpRequest.setName("Test User");
        signUpRequest.setPhone("1234567890");
        signUpRequest.setAddress("123 Test St");
    }

    @Test
    void signUp_Success() {
        when(authRepository.existsByUsername(signUpRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(signUpRequest.getPassword())).thenReturn("encodedPassword");

        userService.signUp(signUpRequest);

        verify(authRepository, times(1)).save(any(Auth.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void signUp_UsernameAlreadyExists() {
        when(authRepository.existsByUsername(signUpRequest.getUsername())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.signUp(signUpRequest));

        verify(authRepository, never()).save(any(Auth.class));
        verify(userRepository, never()).save(any(User.class));
    }
}
