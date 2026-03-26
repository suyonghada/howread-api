package org.howread.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.howread.shared.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * User 도메인 엔티티 (Rich Domain Model).
 *
 * 비즈니스 규칙을 Service가 아닌 엔티티 안에 위치시킨다.
 * Service는 도메인 메서드를 호출하는 조율자 역할만 담당한다.
 *
 * Soft Delete 전략:
 * - deletedAt이 null이면 활성 계정, not null이면 탈퇴 계정.
 * - 물리 삭제 없이 데이터를 보존하여 탈퇴 회원 복구 및 감사 추적이 가능하다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /**
     * S3 오브젝트 키. null이면 프로필 사진 미설정.
     * CloudFront URL은 이 키로 조합하여 생성한다.
     */
    @Column
    private String profileImageKey;

    /** 마지막 로그인 일시. 로그인 성공 시 갱신된다. */
    @Column
    private LocalDateTime lastLoginAt;

    /**
     * Soft Delete 타임스탬프.
     * null이면 활성 계정, not null이면 탈퇴 처리된 계정.
     */
    @Column
    private LocalDateTime deletedAt;

    /**
     * 신규 User를 생성하는 팩토리 메서드.
     * 생성 규칙(기본 권한 MEMBER)을 한 곳에서 관리한다.
     *
     * @param email           이메일 (인증 완료된 값)
     * @param encodedPassword Argon2로 해시된 비밀번호
     * @param nickname        무작위 생성된 닉네임
     */
    public static User create(String email, String encodedPassword, String nickname) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.nickname = nickname;
        user.role = UserRole.MEMBER;
        return user;
    }

    /**
     * 비밀번호를 변경한다.
     * 검증(현재 비밀번호 확인)은 Service에서 수행한 후 이 메서드를 호출한다.
     */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeProfileImageKey(String key) {
        this.profileImageKey = key;
    }

    /** 로그인 성공 시 호출하여 마지막 로그인 일시를 갱신한다. */
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 회원 탈퇴 처리 (Soft Delete).
     * deletedAt에 현재 시각을 기록하며 물리 삭제는 하지 않는다.
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public void changeRole(UserRole newRole) {
        this.role = newRole;
    }
}
