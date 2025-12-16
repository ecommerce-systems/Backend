package com.creepereye.ecommerce.global.security.provider;




import com.creepereye.ecommerce.domain.auth.dto.TokenResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey); // 모든 서비스에서 동일하게 Base64 디코딩
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
    }

    public TokenResponse createToken(Authentication authentication) {
        String accessToken = createAccessToken(authentication);
        String refreshToken = createRefreshToken(authentication);
        return new TokenResponse(accessToken, refreshToken);
    }

    private String createAccessToken(Authentication authentication) {
        return createToken(authentication, accessTokenValidityInMilliseconds);
    }

    private String createRefreshToken(Authentication authentication) {
        return createToken(authentication, refreshTokenValidityInMilliseconds);
    }

    private String createToken(Authentication authentication, long validityInMilliseconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("roles", authorities) // 모든 서비스에서 동일하게 roles 사용
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256) // Access/Refresh 모두 HS256
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature.");
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token.");
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT token compact of handler are invalid.");
        }
        return false;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        String username = claims.getSubject();
        List<SimpleGrantedAuthority> authorities = Stream.of(claims.get("roles").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    public long getRefreshTokenValidityInSeconds() {
        return refreshTokenValidityInMilliseconds / 1000;
    }

    public Long getExpiration(String accessToken) {
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody().getExpiration();
        long now = new Date().getTime();
        return (expiration.getTime() - now);
    }
}
