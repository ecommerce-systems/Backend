package com.creepereye.ecommerce.domain.auth.service;

import com.creepereye.ecommerce.domain.auth.entity.Auth;
import com.creepereye.ecommerce.domain.auth.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthRepository authRepository;

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
