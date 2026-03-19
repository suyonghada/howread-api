package org.howread.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Nested
    @DisplayName("User.create()")
    class Create {

        @Test
        @DisplayName("생성된 User는 MEMBER 권한을 가지며 삭제되지 않은 상태이다")
        void create_defaultRoleIsMemberAndNotDeleted() {
            User user = User.create("test@example.com", "hashed", "HappyTiger042");

            assertThat(user.getRole()).isEqualTo(UserRole.MEMBER);
            assertThat(user.isDeleted()).isFalse();
            assertThat(user.getLastLoginAt()).isNull();
            assertThat(user.getProfileImageKey()).isNull();
        }
    }

    @Nested
    @DisplayName("User.recordLogin()")
    class RecordLogin {

        @Test
        @DisplayName("로그인 기록 시 lastLoginAt이 갱신된다")
        void recordLogin_updatesLastLoginAt() {
            User user = User.create("test@example.com", "hashed", "nickname");

            user.recordLogin();

            assertThat(user.getLastLoginAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("User.delete() — Soft Delete")
    class Delete {

        @Test
        @DisplayName("탈퇴 처리 후 isDeleted()가 true이고 deletedAt이 설정된다")
        void delete_setsDeletedAtAndIsDeletedReturnsTrue() {
            User user = User.create("test@example.com", "hashed", "nickname");

            user.delete();

            assertThat(user.isDeleted()).isTrue();
            assertThat(user.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("User.changeNickname()")
    class ChangeNickname {

        @Test
        @DisplayName("닉네임 변경 후 새 닉네임이 적용된다")
        void changeNickname_updatesNickname() {
            User user = User.create("test@example.com", "hashed", "OldNickname");

            user.changeNickname("NewNickname");

            assertThat(user.getNickname()).isEqualTo("NewNickname");
        }
    }

    @Nested
    @DisplayName("User.changePassword()")
    class ChangePassword {

        @Test
        @DisplayName("비밀번호 변경 후 새 해시가 적용된다")
        void changePassword_updatesPassword() {
            User user = User.create("test@example.com", "oldHash", "nickname");

            user.changePassword("newHash");

            assertThat(user.getPassword()).isEqualTo("newHash");
        }
    }

    @Nested
    @DisplayName("UserRole.toGrantedAuthority()")
    class GrantedAuthority {

        @Test
        @DisplayName("MEMBER 권한은 ROLE_MEMBER로 변환된다")
        void toGrantedAuthority_memberRole() {
            assertThat(UserRole.MEMBER.toGrantedAuthority()).isEqualTo("ROLE_MEMBER");
        }

        @Test
        @DisplayName("ADMIN 권한은 ROLE_ADMIN으로 변환된다")
        void toGrantedAuthority_adminRole() {
            assertThat(UserRole.ADMIN.toGrantedAuthority()).isEqualTo("ROLE_ADMIN");
        }
    }
}
