package com.creepereye.ecommerce.domain.auth.service;

import com.creepereye.ecommerce.domain.auth.dto.LoginRequest;
import com.creepereye.ecommerce.domain.auth.dto.TokenResponse;
import com.creepereye.ecommerce.domain.auth.repository.AuthRepository;
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

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceV2Test {

    @Mock
    private AuthRepository authRepository;
    @Mock
    private RedisService redisService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceV2 authService;

    @Test
    @DisplayName("V2 로그인 시, Refresh Token이 Redis에 저장된다")
    void login_shouldSaveTokenToRedis() {
        LoginRequest request = new LoginRequest("user", "password");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user");
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        
        TokenResponse tokenResponse = new TokenResponse("access", "refresh");
        when(jwtTokenProvider.createToken(authentication)).thenReturn(tokenResponse);
        when(jwtTokenProvider.getRefreshTokenValidityInSeconds()).thenReturn(3600L);

        authService.login(request);

        verify(redisService).setValues(eq("user"), eq("refresh"), anyLong(), eq(TimeUnit.SECONDS));
    }
}
