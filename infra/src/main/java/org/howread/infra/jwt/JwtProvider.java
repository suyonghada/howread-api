package org.howread.infra.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.howread.common.exception.BusinessException;
import org.howread.user.application.UserErrorCode;
import org.howread.user.application.port.JwtPort;
import org.howread.user.domain.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * [Adapter] JwtPort를 jjwt 라이브러리로 구현.
 *
 * 토큰 생성·검증의 기술 세부사항을 캡슐화한다.
 * 알고리즘 교체나 클레임 구조 변경 시 이 클래스만 수정한다.
 *
 * 토큰 구조:
 * - AccessToken:  subject=userId, claim "role"=UserRole.name(), 유효기간 6시간
 * - RefreshToken: subject=userId, 유효기간 7일 (role 클레임 없음)
 */
@Slf4j
@Component
public class JwtProvider implements JwtPort {

    private static final String ROLE_CLAIM = "role";

    private final SecretKey secretKey;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry}") long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    @Override
    public String generateAccessToken(Long userId, UserRole role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiry * 1000);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiry * 1000);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public Long extractUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    @Override
    public UserRole extractRole(String token) {
        String role = parseClaims(token).get(ROLE_CLAIM, String.class);
        return UserRole.valueOf(role);
    }

    @Override
    public LocalDateTime extractRefreshTokenExpiresAt(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
    }

    @Override
    public void validate(String token) {
        parseClaims(token);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(UserErrorCode.TOKEN_EXPIRED, e);
        } catch (JwtException e) {
            throw new BusinessException(UserErrorCode.INVALID_TOKEN, e);
        }
    }
}
