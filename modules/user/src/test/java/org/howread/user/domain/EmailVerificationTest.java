package org.howread.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EmailVerificationTest {

    @Nested
    @DisplayName("EmailVerification.create()")
    class Create {

        @Test
        @DisplayName("생성 시 verified=false, expiresAt은 now+10분이다")
        void create_defaultState() {
            LocalDateTime before = LocalDateTime.now();
            EmailVerification ev = EmailVerification.create("test@example.com", "123456");
            LocalDateTime after = LocalDateTime.now();

            assertThat(ev.isVerified()).isFalse();
            assertThat(ev.getEmail()).isEqualTo("test@example.com");
            assertThat(ev.getCode()).isEqualTo("123456");
            assertThat(ev.getExpiresAt()).isAfterOrEqualTo(before.plusMinutes(9));
            assertThat(ev.getExpiresAt()).isBeforeOrEqualTo(after.plusMinutes(10).plusSeconds(1));
        }
    }

    @Nested
    @DisplayName("EmailVerification.isCodeMatch()")
    class IsCodeMatch {

        @Test
        @DisplayName("동일한 코드는 true를 반환한다")
        void isCodeMatch_sameCode_returnsTrue() {
            EmailVerification ev = EmailVerification.create("test@example.com", "123456");
            assertThat(ev.isCodeMatch("123456")).isTrue();
        }

        @Test
        @DisplayName("다른 코드는 false를 반환한다")
        void isCodeMatch_differentCode_returnsFalse() {
            EmailVerification ev = EmailVerification.create("test@example.com", "123456");
            assertThat(ev.isCodeMatch("654321")).isFalse();
        }
    }

    @Nested
    @DisplayName("EmailVerification.isExpired()")
    class IsExpired {

        @Test
        @DisplayName("만료 전이면 false를 반환한다")
        void isExpired_beforeExpiry_returnsFalse() {
            EmailVerification ev = EmailVerification.create("test@example.com", "123456");
            assertThat(ev.isExpired()).isFalse();
        }

        @Test
        @DisplayName("만료 후이면 true를 반환한다")
        void isExpired_afterExpiry_returnsTrue() throws Exception {
            EmailVerification ev = EmailVerification.create("test@example.com", "123456");

            Field field = EmailVerification.class.getDeclaredField("expiresAt");
            field.setAccessible(true);
            field.set(ev, LocalDateTime.now().minusSeconds(1));

            assertThat(ev.isExpired()).isTrue();
        }
    }

    @Nested
    @DisplayName("EmailVerification.verify()")
    class Verify {

        @Test
        @DisplayName("verify() 호출 후 verified가 true로 변경된다")
        void verify_setsVerifiedTrue() {
            EmailVerification ev = EmailVerification.create("test@example.com", "123456");

            ev.verify();

            assertThat(ev.isVerified()).isTrue();
        }
    }
}
