package com.creepereye.ecommerce.domain.auth.controller;

import com.creepereye.ecommerce.domain.auth.dto.LoginRequest;
import com.creepereye.ecommerce.domain.auth.dto.PasswordChangeRequest;
import com.creepereye.ecommerce.domain.auth.dto.SignUpRequest;
import com.creepereye.ecommerce.domain.auth.dto.TokenResponse;
import com.creepereye.ecommerce.domain.auth.service.AuthServiceV2;
import com.creepereye.ecommerce.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthControllerV2 {

    private final AuthServiceV2 authService;
    private final UserService userService;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignUpRequest signUpRequest) {
        userService.signUp(signUpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup-admin")
    public ResponseEntity<Void> signupAdmin(@RequestBody SignUpRequest signUpRequest) {
        userService.signUpAdmin(signUpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {

        TokenResponse tokenResponse = authService.login(request);
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokenResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/api/v2/auth/refresh")
                .maxAge(refreshTokenValidityInSeconds)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String accessToken) {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            authService.logout(accessToken.substring(7));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@CookieValue("refreshToken") String refreshToken) {
        TokenResponse tokenResponse = authService.refresh(refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordChangeRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAccount() {
        userService.deleteAccount();
        return ResponseEntity.ok().build();
    }
}
