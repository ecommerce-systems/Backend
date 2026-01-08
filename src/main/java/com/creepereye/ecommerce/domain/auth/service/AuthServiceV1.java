package com.creepereye.ecommerce.domain.auth.service;

import com.creepereye.ecommerce.domain.auth.dto.LoginRequest;
import com.creepereye.ecommerce.domain.auth.dto.PasswordChangeRequest;
import com.creepereye.ecommerce.domain.auth.dto.TokenResponse;
import com.creepereye.ecommerce.domain.auth.entity.Auth;
import com.creepereye.ecommerce.domain.auth.entity.RefreshToken;
import com.creepereye.ecommerce.domain.auth.repository.AuthRepository;
import com.creepereye.ecommerce.domain.auth.repository.RefreshTokenRepository;
import com.creepereye.ecommerce.global.security.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceV1 {

    private final AuthRepository authRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        TokenResponse tokenResponse = jwtTokenProvider.createToken(authentication);

        saveRefreshToken(authentication.getName(), tokenResponse.getRefreshToken());

        return tokenResponse;
    }

    @Transactional
    public void logout(String accessToken) {
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new IllegalArgumentException("Invalid token");
        }
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        refreshTokenRepository.deleteByUsername(authentication.getName());
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

        RefreshToken savedToken = refreshTokenRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
                
        if (!refreshToken.equals(savedToken.getToken())) {
             throw new IllegalArgumentException("Mismatched refresh token");
        }
        
        if (savedToken.getExpiryDate() < System.currentTimeMillis()) {
            refreshTokenRepository.delete(savedToken);
             throw new IllegalArgumentException("Refresh token expired");
        }

        TokenResponse tokenResponse = jwtTokenProvider.createToken(authentication);

        saveRefreshToken(authentication.getName(), tokenResponse.getRefreshToken());

        return tokenResponse;
    }

    private void saveRefreshToken(String username, String token) {
        refreshTokenRepository.findByUsername(username).ifPresent(refreshTokenRepository::delete);
        
        long validity = jwtTokenProvider.getRefreshTokenValidityInSeconds() * 1000;
        RefreshToken refreshToken = RefreshToken.builder()
                .username(username)
                .token(token)
                .expiryDate(System.currentTimeMillis() + validity)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        String username = getCurrentUsername();
        Auth auth = authRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(request.getOldPassword(), auth.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        auth.setPassword(passwordEncoder.encode(request.getNewPassword()));
        authRepository.save(auth);
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }
}
