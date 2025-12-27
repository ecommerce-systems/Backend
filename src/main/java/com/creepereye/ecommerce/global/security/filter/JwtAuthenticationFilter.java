package com.creepereye.ecommerce.global.security.filter;



import com.creepereye.ecommerce.domain.auth.service.RedisService;
import com.creepereye.ecommerce.global.security.provider.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration; // Add this import

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;


    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RedisService redisService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisService = redisService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            logger.debug("Headers 📋 {}: {}", headerName, request.getHeader(headerName));
        }

        String token = resolveToken(request);
        logger.debug("➡️ Authorization header: {}", request.getHeader("Authorization"));
        logger.debug("🔑 Resolved token: {}", token);

        if (StringUtils.hasText(token)) {
            boolean isValid = jwtTokenProvider.validateToken(token);
            logger.debug("✅ Is token valid? {}", isValid);

            if (isValid) {
                if (redisService.getValues(token) == null) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("✨ Authentication set for user: {}", authentication.getName());
                } else {
                    logger.warn("🚫 Invalid JWT token: This token is blacklisted");
                }
            } else {
                logger.warn("❌ JWT token validation failed for token: {}", token);
            }
        } else {
            logger.debug("❓ No JWT token found in request");
        }


        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Bearer token: {}", bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
