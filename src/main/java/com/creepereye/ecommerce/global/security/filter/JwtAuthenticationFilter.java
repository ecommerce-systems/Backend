package com.creepereye.ecommerce.global.security.filter;

import com.creepereye.ecommerce.domain.auth.service.RedisService;
import com.creepereye.ecommerce.global.security.provider.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Added import
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; // Added import
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

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


            logger.error("©️{}",request);
            logger.debug("--- Start doFilterInternal for request URI: {} ---", request.getRequestURI());

            Authentication initialAuth = SecurityContextHolder.getContext().getAuthentication();

            if (initialAuth != null) {

                logger.debug("Initial SecurityContext Authentication: Name='{}', Authenticated={}, Authorities={}",

                        initialAuth.getName(), initialAuth.isAuthenticated(), initialAuth.getAuthorities());

            } else {

                logger.debug("Initial SecurityContext Authentication: null");

            }



            // 모든 헤더 출력

            Enumeration<String> headerNames = request.getHeaderNames();

            while (headerNames.hasMoreElements()) {

                String headerName = headerNames.nextElement();

                // log.debug("📋 Header {}: {}", headerName, request.getHeader(headerName)); // Original line

                // Mask Authorization header for security

                if ("authorization".equalsIgnoreCase(headerName)) {

                    String authHeader = request.getHeader(headerName);

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {

                        logger.debug("📋 Header {}: Bearer <token_masked>", headerName);

                    } else {

                        logger.debug("📋 Header {}: {}", headerName, authHeader);

                    }

                } else {

                    logger.debug("📋 Header {}: {}", headerName, request.getHeader(headerName));

                }

            }



            String token = resolveToken(request);
            logger.debug("--- End doFilterInternal for request URI: {}", request);

            logger.debug("➡️ Authorization header (resolved by getHeader(\"Authorization\")): {}", request.getHeader("Authorization"));

            logger.debug("🔑 Resolved token (after substring): {}", token != null ? "found" : "null");

            logger.debug("➡️ Raw headers (from Enumeration): " + Collections.list(request.getHeaderNames()));





            if (StringUtils.hasText(token)) {

                boolean isValid = jwtTokenProvider.validateToken(token);

                logger.debug("✅ Is token valid? {}", isValid);



                if (isValid) {

                    // Check if token is blacklisted in Redis

                    if (redisService.getValues(token) == null) {

                        Authentication authentication = jwtTokenProvider.getAuthentication(token);
                        // Ensure it's a UsernamePasswordAuthenticationToken to set details
                        if (authentication instanceof UsernamePasswordAuthenticationToken) {
                            UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) authentication;
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // Added line
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            logger.info("✨ Authentication set for user: '{}', Authenticated={}, Authorities={}",
                                    authentication.getName(), authentication.isAuthenticated(), authentication.getAuthorities());
                        } else {
                            logger.warn("Authentication object is not UsernamePasswordAuthenticationToken, cannot set details.");
                            SecurityContextHolder.getContext().setAuthentication(authentication); // Fallback
                        }
                    } else {

                        logger.warn("🚫 Invalid JWT token: This token is blacklisted");

                    }

                } else {

                    logger.warn("❌ JWT token validation failed for token: {}", token);

                }

            } else {

                logger.debug("❓ No JWT token found in request or token is empty");

            }



            Authentication finalAuth = SecurityContextHolder.getContext().getAuthentication();

            if (finalAuth != null) {

                logger.debug("Final SecurityContext Authentication: Name='{}', Authenticated={}, Authorities={}",

                        finalAuth.getName(), finalAuth.isAuthenticated(), finalAuth.getAuthorities());

            } else {

                logger.debug("Final SecurityContext Authentication: null");

            }

            logger.debug("--- End doFilterInternal for request URI: {} ---", request.getRequestURI());



            filterChain.doFilter(request, response);

        }



        private String resolveToken(HttpServletRequest request) {
            logger.debug("Bearer token raw: {}", request);
            String bearerToken = request.getHeader("Authorization");

             logger.debug("🦾Bearer token raw: {}", bearerToken);

            // Mask Authorization header for security

            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {

                logger.debug("Bearer token raw: Bearer <token_masked>");

                return bearerToken.substring(7);

            }

            logger.debug("Bearer token raw: null or not Bearer token");

            return null;

        }

    }

