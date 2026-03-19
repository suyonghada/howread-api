package org.howread.user.application.port;

import org.howread.user.domain.RefreshToken;

import java.util.Optional;

/**
 * [Port] Refresh Token 저장소 계약.
 *
 * 도메인이 외부 저장소에 요청하는 계약만 정의한다.
 * 구현체는 infra 모듈의 RefreshTokenRepositoryAdapter에 위치한다.
 */
public interface RefreshTokenRepository {

    Optional<RefreshToken> findByToken(String token);

    /** 로그아웃·회원탈퇴 시 해당 사용자의 모든 Refresh Token을 삭제한다. */
    void deleteByUserId(Long userId);

    RefreshToken save(RefreshToken refreshToken);
}
