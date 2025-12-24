package com.creepereye.ecommerce.domain.user.controller;

import com.creepereye.ecommerce.domain.user.dto.UserResponse;
import com.creepereye.ecommerce.domain.user.dto.UserUpdateRequest;
import com.creepereye.ecommerce.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getUserInfo() {
        UserResponse userInfo = userService.getUserInfo();
        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateUserInfo(@RequestBody UserUpdateRequest updateRequest) {
        userService.updateUserInfo(updateRequest);
        return ResponseEntity.ok().build();
    }
}
