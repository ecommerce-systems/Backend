package com.creepereye.ecommerce.domain.auth.service;


import com.creepereye.ecommerce.domain.auth.dto.LoginRequest;
import com.creepereye.ecommerce.domain.auth.dto.PasswordChangeRequest;
import com.creepereye.ecommerce.domain.auth.dto.TokenResponse;
import com.creepereye.ecommerce.domain.auth.entity.Auth;
import com.creepereye.ecommerce.domain.auth.repository.AuthRepository;
import com.creepereye.ecommerce.global.security.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final AuthRepository authRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        TokenResponse tokenResponse = jwtTokenProvider.createToken(authentication);

        redisService.setValues(
                authentication.getName(),
                tokenResponse.getRefreshToken(),
                jwtTokenProvider.getRefreshTokenValidityInSeconds(),
                TimeUnit.SECONDS
        );

        return tokenResponse;
    }

    public void logout(String accessToken) {
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new IllegalArgumentException("Invalid token");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);

        if (redisService.getValues(authentication.getName()) != null) {
            redisService.deleteValues(authentication.getName());
        }

        Long expiration = jwtTokenProvider.getExpiration(accessToken);
        redisService.setValues(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

        String savedRefreshToken = redisService.getValues(authentication.getName());
        if (!refreshToken.equals(savedRefreshToken)) {
            throw new IllegalArgumentException("Mismatched refresh token");
        }

        TokenResponse tokenResponse = jwtTokenProvider.createToken(authentication);

        redisService.setValues(
                authentication.getName(),
                tokenResponse.getRefreshToken(),
                jwtTokenProvider.getRefreshTokenValidityInSeconds(),
                TimeUnit.SECONDS
        );

        Long expiration = jwtTokenProvider.getExpiration(refreshToken);
        redisService.setValues(refreshToken, "logout", expiration, TimeUnit.MILLISECONDS);

        return tokenResponse;
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


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Auth auth = authRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(auth.getUsername())
                .password(auth.getPassword())
                .roles(auth.getRoles().toArray(new String[0]))
                .build();
    }
}