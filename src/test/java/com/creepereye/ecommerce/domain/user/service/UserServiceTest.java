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
    @DisplayName("회원가입 시, Auth와 User 엔티티가 모두 저장된다")
    void signUp_shouldSaveAuthAndUser() {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("testUser");
        request.setPassword("password");

        when(authRepository.existsByUsername("testUser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        userService.signUp(request);

        verify(authRepository).save(any(Auth.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 시, 동일한 아이디가 존재하면 예외가 발생한다")
    void signUp_shouldThrowException_whenUsernameExists() {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("testUser");

        when(authRepository.existsByUsername("testUser")).thenReturn(true);

        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("내 정보 조회 시, 현재 로그인된 사용자의 정보를 반환한다")
    void getUserInfo_shouldReturnCurrentUserInfo() {
        setupSecurityContext();
        when(userRepository.findByAuthUsername("testUser")).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserInfo();

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("내 정보 수정 시, 요청된 필드가 업데이트된다")
    void updateUserInfo_shouldUpdateFields() {
        setupSecurityContext();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setPhone("010-9999-9999");
        request.setAddress("New Address");

        when(userRepository.findByAuthUsername("testUser")).thenReturn(Optional.of(user));

        userService.updateUserInfo(request);

        assertThat(user.getPhone()).isEqualTo("010-9999-9999");
        assertThat(user.getAddress()).isEqualTo("New Address");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("회원 탈퇴 시, User와 Auth 정보가 모두 삭제된다")
    void deleteAccount_shouldDeleteUserAndAuth() {
        setupSecurityContext();
        when(userRepository.findByAuthUsername("testUser")).thenReturn(Optional.of(user));

        userService.deleteAccount();

        verify(userRepository).delete(user);
        verify(authRepository).delete(auth);
    }
}
