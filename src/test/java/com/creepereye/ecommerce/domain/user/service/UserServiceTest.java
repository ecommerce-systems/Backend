package com.creepereye.ecommerce.domain.user.service;

import com.creepereye.ecommerce.domain.auth.dto.SignUpRequest;
import com.creepereye.ecommerce.domain.auth.entity.Auth;
import com.creepereye.ecommerce.domain.auth.repository.AuthRepository;
import com.creepereye.ecommerce.domain.user.dto.UserResponse;
import com.creepereye.ecommerce.domain.user.dto.UserUpdateRequest;
import com.creepereye.ecommerce.domain.user.entity.User;
import com.creepereye.ecommerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    private User user;
    private Auth auth;

    @BeforeEach
    void setUp() {
        // Only set up security context for tests that need it?
        // Or do it in @Test methods manually if not all need it.
        // signUp does not need it. getUserInfo needs it.
        // Let's create helper method or do it in test.
        auth = Auth.builder().username("testUser").build();
        user = User.builder().id(1L).auth(auth).build();
    }

    private void setupSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getUsername()).thenReturn("testUser");

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("signUp should save Auth and User")
    void signUp_shouldSaveUser() {
        // Given
        SignUpRequest request = new SignUpRequest();
        request.setUsername("testUser");
        request.setPassword("password");
        request.setName("Name");
        request.setPhone("123");
        request.setAddress("Addr");

        when(authRepository.existsByUsername("testUser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        // When
        userService.signUp(request);

        // Then
        verify(authRepository).save(any(Auth.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("signUp should throw exception if username exists")
    void signUp_shouldThrowException_whenUsernameExists() {
        // Given
        SignUpRequest request = new SignUpRequest();
        request.setUsername("testUser");
        request.setPassword("password");
        request.setName("Name");
        request.setPhone("123");
        request.setAddress("Addr");

        when(authRepository.existsByUsername("testUser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getUserInfo should return user info")
    void getUserInfo_shouldReturnUserInfo() {
        // Given
        setupSecurityContext();
        when(userRepository.findByAuthUsername("testUser")).thenReturn(Optional.of(user));

        // When
        UserResponse response = userService.getUserInfo();

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("updateUserInfo should update fields")
    void updateUserInfo_shouldUpdateFields() {
        // Given
        setupSecurityContext();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setPhone("999");
        request.setAddress("New Addr");

        when(userRepository.findByAuthUsername("testUser")).thenReturn(Optional.of(user));

        // When
        userService.updateUserInfo(request);

        // Then
        assertThat(user.getPhone()).isEqualTo("999");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("deleteAccount should delete user and auth")
    void deleteAccount_shouldDelete() {
        // Given
        setupSecurityContext();
        when(userRepository.findByAuthUsername("testUser")).thenReturn(Optional.of(user));

        // When
        userService.deleteAccount();

        // Then
        verify(userRepository).delete(user);
        verify(authRepository).delete(auth);
    }
}
