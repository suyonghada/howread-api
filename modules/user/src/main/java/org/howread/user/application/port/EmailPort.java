package org.howread.user.application.port;

/**
 * [Port] 이메일 발송 계약.
 *
 * 도메인이 JavaMailSender 등 이메일 기술 세부사항에 의존하지 않도록 인터페이스로 추상화한다.
 * 구현체는 infra 모듈의 EmailService에 위치한다.
 */
public interface EmailPort {

    void sendVerificationCode(String to, String code);

    void sendPasswordResetCode(String to, String code);
}
