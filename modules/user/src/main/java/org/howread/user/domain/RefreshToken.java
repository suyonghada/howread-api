package org.howread.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Refresh Token 엔티티.
 *
 * JWT Refresh Token을 DB에 저장하여 토큰 탈취·무효화(로그아웃, 회원탈퇴) 시
 * 서버 측에서 즉시 폐기할 수 있도록 한다.
 *
 * userId는 FK가 아닌 단순 Long 참조로 저장한다.
 * User 엔티티와의 직접 연관을 끊어 독립적인 생명주기를 유지하고,
 * 대량 삭제(deleteByUserId) 시 JPA 연관 로드 없이 처리할 수 있다.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public static RefreshToken create(Long userId, String token, LocalDateTime expiresAt) {
        RefreshToken rt = new RefreshToken();
        rt.userId = userId;
        rt.token = token;
        rt.expiresAt = expiresAt;
        return rt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
