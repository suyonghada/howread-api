package org.howread.infra.user;

import lombok.RequiredArgsConstructor;
import org.howread.user.application.port.RefreshTokenRepository;
import org.howread.user.domain.RefreshToken;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * [Adapter] RefreshTokenRepository Port를 JPA로 구현.
 *
 * 기술 교체 시 이 클래스만 변경한다.
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token);
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return jpaRepository.save(refreshToken);
    }
}
