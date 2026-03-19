package org.howread.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    @DisplayName("ErrorCode로 생성 시 메시지와 에러코드가 올바르게 설정된다")
    void constructor_withErrorCode_setsMessageAndErrorCode() {
        BusinessException exception = new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);

        assertThat(exception.getMessage()).isEqualTo(CommonErrorCode.RESOURCE_NOT_FOUND.getMessage());
        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.RESOURCE_NOT_FOUND);
        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("cause와 함께 생성 시 원인 예외가 연결된다")
    void constructor_withCause_chainsCause() {
        RuntimeException cause = new RuntimeException("DB connection failed");
        BusinessException exception = new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR, cause);

        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("BusinessException은 RuntimeException을 상속한다")
    void businessException_isRuntimeException() {
        BusinessException exception = new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
