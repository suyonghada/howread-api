package org.howread.user.application.port;

import org.howread.user.domain.UserRole;

import java.time.LocalDateTime;

/**
 * [Port] JWT 토큰 생성 계약.
 *
 * 도메인이 JWT 라이브러리(jjwt 등)에 직접 의존하지 않도록 인터페이스로 추상화한다.
 * 구현체는 infra 모듈의 JwtProvider에 위치한다.
 */
public interface JwtPort {

    String generateAccessToken(Long userId, UserRole role);

    String generateRefreshToken(Long userId);

    Long extractUserId(String token);

    /** 토큰 만료 시각. RefreshToken 엔티티 생성 시 expiresAt 계산에 사용. */
    LocalDateTime extractRefreshTokenExpiresAt(String token);

    /**
     * 토큰을 파싱하고 유효성을 검증한다.
     * 만료되거나 위조된 토큰이면 BusinessException을 발생시킨다.
     */
    void validate(String token);
}
