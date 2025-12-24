package com.creepereye.ecommerce.domain.user.controller;

import com.creepereye.ecommerce.domain.auth.entity.Auth;
import com.creepereye.ecommerce.domain.auth.repository.AuthRepository;
import com.creepereye.ecommerce.domain.auth.service.RedisService;
import com.creepereye.ecommerce.domain.product.repository.ProductRepository;
import com.creepereye.ecommerce.domain.user.dto.UserUpdateRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration")
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class UserControllerTest {

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
    void getUserInfo_ShouldReturnUserInfo() throws Exception {
        Auth auth = Auth.builder().username("testuser").build();
        User user = User.builder()
                .name("Test User")
                .phone("1234567890")
                .address("123 Test St")
                .auth(auth)
                .build();
        when(userRepository.findByAuthUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateUserInfo_ShouldReturnOk() throws Exception {
        Auth auth = Auth.builder().username("testuser").build();
        User user = User.builder().auth(auth).build();
        when(userRepository.findByAuthUsername("testuser")).thenReturn(Optional.of(user));

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setPhone("0987654321");
        updateRequest.setAddress("321 Test St");

        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserInfo_Unauthenticated_ShouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
