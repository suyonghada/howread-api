package org.howread.user.domain;

/**
 * 사용자 권한 열거형.
 *
 * 추후 권한이 추가되면 이 enum에만 항목을 추가한다.
 * Spring Security의 GrantedAuthority 변환 메서드를 엔티티 수준에서 제공하여
 * Service 계층이 Spring Security 타입에 직접 의존하지 않도록 한다.
 */
public enum UserRole {
    MEMBER, ADMIN;

    public String toGrantedAuthority() {
        return "ROLE_" + this.name();
    }
}
