package org.howread.infra.user;

import org.howread.user.domain.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * EmailVerification JPA Repository.
 * 재발송 시 신규 레코드를 생성하므로 가장 최근 레코드를 기준으로 조회한다.
 */
public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByEmailOrderByIdDesc(String email);

    void deleteByEmail(String email);
}
