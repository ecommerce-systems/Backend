package com.creepereye.ecommerce.domain.auth.controller;

import com.creepereye.ecommerce.domain.auth.dto.PasswordChangeRequest;
import com.creepereye.ecommerce.domain.auth.entity.Auth;
import com.creepereye.ecommerce.domain.auth.repository.AuthRepository;
import com.creepereye.ecommerce.domain.auth.service.RedisService;
import com.creepereye.ecommerce.domain.product.repository.ProductRepository;
import com.creepereye.ecommerce.domain.user.entity.User;
import com.creepereye.ecommerce.domain.user.repository.UserRepository;
import com.creepereye.ecommerce.global.security.provider.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration")
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthRepository authRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RedisService redisService;

    @MockBean
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Test
    @WithMockUser(username = "testuser")
    void changePassword_ShouldReturnOk() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        Auth mockAuth = Auth.builder().username("testuser").password("encodedOldPass").build();
        when(authRepository.findByUsername("testuser")).thenReturn(Optional.of(mockAuth));
        when(passwordEncoder.matches(request.getOldPassword(), "encodedOldPass")).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_Unauthenticated_ShouldBeUnauthorized() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        mockMvc.perform(post("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteAccount_ShouldReturnOk() throws Exception {
        Auth mockAuth = Auth.builder().username("testuser").build();
        User mockUser = User.builder().auth(mockAuth).build();
        when(userRepository.findByAuthUsername("testuser")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(delete("/api/v1/auth/me"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteAccount_Unauthenticated_ShouldBeUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
