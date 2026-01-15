package com.creepereye.ecommerce.domain.auth.service;

import com.creepereye.ecommerce.domain.auth.dto.LoginRequest;
import com.creepereye.ecommerce.domain.auth.dto.TokenResponse;
import com.creepereye.ecommerce.domain.auth.entity.RefreshToken;
import com.creepereye.ecommerce.domain.auth.repository.AuthRepository;
import com.creepereye.ecommerce.domain.auth.repository.RefreshTokenRepository;
import com.creepereye.ecommerce.global.security.provider.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceV1Test {

    @Mock
    private AuthRepository authRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceV1 authService;

    @Test
    @DisplayName("V1 로그인 시, Refresh Token이 DB에 저장된다")
    void login_shouldSaveTokenToDB() {
        LoginRequest request = new LoginRequest("user", "password");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user");
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        
        TokenResponse tokenResponse = new TokenResponse("access", "refresh");
        when(jwtTokenProvider.createToken(authentication)).thenReturn(tokenResponse);
        when(jwtTokenProvider.getRefreshTokenValidityInSeconds()).thenReturn(3600L);

        authService.login(request);

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }
    
    @Test
    @DisplayName("V1 토큰 갱신 시, 유효성 검증 후 새 토큰을 발급한다")
    void refresh_shouldIssueNewToken() {
        String refreshToken = "valid_refresh";
        String username = "user";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(refreshToken)).thenReturn(authentication);
        
        RefreshToken storedToken = RefreshToken.builder()
                .token(refreshToken)
                .username(username)
                .expiryDate(System.currentTimeMillis() + 10000)
                .build();
        when(refreshTokenRepository.findByUsername(username)).thenReturn(Optional.of(storedToken));
        
        TokenResponse newToken = new TokenResponse("new_access", "new_refresh");
        when(jwtTokenProvider.createToken(authentication)).thenReturn(newToken);
        when(jwtTokenProvider.getRefreshTokenValidityInSeconds()).thenReturn(3600L);

        authService.refresh(refreshToken);

        verify(refreshTokenRepository, atLeastOnce()).save(any(RefreshToken.class));
    }
}
