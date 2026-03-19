package org.howread.user.application.port;

import org.howread.user.domain.EmailVerification;

import java.util.Optional;

/**
 * [Port] 이메일 인증 저장소 계약.
 *
 * 도메인이 외부 저장소에 요청하는 계약만 정의한다.
 * 구현체는 infra 모듈의 EmailVerificationRepositoryAdapter에 위치한다.
 */
public interface EmailVerificationRepository {

    /**
     * 해당 이메일로 발급된 가장 최근 인증 레코드를 조회한다.
     * 재발송 시 이전 코드를 덮어쓰지 않고 신규 레코드를 생성하므로,
     * 항상 최신 레코드를 기준으로 검증한다.
     */
    Optional<EmailVerification> findLatestByEmail(String email);

    EmailVerification save(EmailVerification emailVerification);
}
