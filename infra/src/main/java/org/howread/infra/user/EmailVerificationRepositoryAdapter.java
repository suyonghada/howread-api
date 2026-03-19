package org.howread.infra.user;

import lombok.RequiredArgsConstructor;
import org.howread.user.application.port.EmailVerificationRepository;
import org.howread.user.domain.EmailVerification;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * [Adapter] EmailVerificationRepository Port를 JPA로 구현.
 *
 * 기술 교체 시 이 클래스만 변경한다.
 */
@Repository
@RequiredArgsConstructor
public class EmailVerificationRepositoryAdapter implements EmailVerificationRepository {

    private final EmailVerificationJpaRepository jpaRepository;

    @Override
    public Optional<EmailVerification> findLatestByEmail(String email) {
        return jpaRepository.findTopByEmailOrderByIdDesc(email);
    }

    @Override
    public EmailVerification save(EmailVerification emailVerification) {
        return jpaRepository.save(emailVerification);
    }

    @Override
    public void deleteByEmail(String email) {
        jpaRepository.deleteByEmail(email);
    }
}
