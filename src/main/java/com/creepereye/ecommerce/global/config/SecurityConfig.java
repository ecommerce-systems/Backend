package com.creepereye.ecommerce.global.config;

import com.creepereye.ecommerce.domain.auth.service.RedisService;
import com.creepereye.ecommerce.global.security.filter.JwtAuthenticationFilter;
import com.creepereye.ecommerce.global.security.handler.JwtAuthenticationEntryPoint;
import com.creepereye.ecommerce.global.security.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;


import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final AccessDeniedHandler customAccessDeniedHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("api/v1/auth/login").permitAll()
                        .requestMatchers("api/v1/auth/logout").authenticated()
                        .requestMatchers("api/v1/auth/refresh").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, redisService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
