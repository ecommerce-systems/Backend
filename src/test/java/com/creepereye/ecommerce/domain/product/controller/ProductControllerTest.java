package com.creepereye.ecommerce.domain.product.controller;

import com.creepereye.ecommerce.domain.auth.repository.AuthRepository;
import com.creepereye.ecommerce.domain.auth.service.RedisService;
import com.creepereye.ecommerce.domain.product.entity.Product;
import com.creepereye.ecommerce.domain.product.repository.ProductRepository;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration")
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class ProductControllerTest {

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
    @WithMockUser(roles = "USER")
    void createProduct_AsUser_ShouldBeForbidden() throws Exception {
        Product product = new Product();
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_AsAdmin_ShouldBeCreated() throws Exception {
        Product product = new Product();
        when(productRepository.save(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void getProduct_AsUser_ShouldBeOk() throws Exception {
        when(productRepository.findById(1)).thenReturn(Optional.of(new Product()));
        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_AsAdmin_ShouldBeNoContent() throws Exception {
        when(productRepository.findById(1)).thenReturn(Optional.of(new Product()));
        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteProduct_AsUser_ShouldBeForbidden() throws Exception {
        when(productRepository.findById(1)).thenReturn(Optional.of(new Product()));
        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isForbidden());
    }
}
