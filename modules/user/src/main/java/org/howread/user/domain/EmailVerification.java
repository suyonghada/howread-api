package org.howread.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이메일 인증 엔티티.
 *
 * 회원가입 및 비밀번호 재설정 시 발급하는 6자리 인증코드를 관리한다.
 * 인증코드는 발급 후 10분간 유효하며, 검증 성공 시 verified = true로 변경된다.
 */
@Entity
@Table(name = "email_verifications", indexes = {
        @Index(name = "idx_email_verifications_email", columnList = "email")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean verified;

    private static final long EXPIRY_MINUTES = 10;

    /**
     * 인증 코드를 발급한다.
     * 만료 시각은 발급 시점 +10분으로 설정된다.
     */
    public static EmailVerification create(String email, String code) {
        EmailVerification ev = new EmailVerification();
        ev.email = email;
        ev.code = code;
        ev.expiresAt = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);
        ev.verified = false;
        return ev;
    }

    /** 인증 완료 처리. verified = true로 변경한다. */
    public void verify() {
        this.verified = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isCodeMatch(String input) {
        return this.code.equals(input);
    }
}
