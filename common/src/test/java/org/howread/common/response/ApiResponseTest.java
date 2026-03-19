package org.howread.common.response;

import org.howread.common.exception.CommonErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Nested
    @DisplayName("success(T data)")
    class SuccessWithData {

        @Test
        @DisplayName("success 플래그가 true이고 data가 설정되며 error는 null이다")
        void success_withData_setsSuccessFlagAndData() {
            String data = "hello";
            ApiResponse<String> response = ApiResponse.success(data);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo(data);
            assertThat(response.getError()).isNull();
        }
    }

    @Nested
    @DisplayName("success()")
    class SuccessWithoutData {

        @Test
        @DisplayName("success 플래그가 true이고 data와 error가 모두 null이다")
        void success_noData_onlyFlagIsTrue() {
            ApiResponse<Void> response = ApiResponse.success();

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isNull();
            assertThat(response.getError()).isNull();
        }
    }

    @Nested
    @DisplayName("fail(ErrorCode)")
    class FailWithErrorCode {

        @Test
        @DisplayName("success 플래그가 false이고 error에 코드와 메시지가 설정된다")
        void fail_withErrorCode_setsErrorFields() {
            ApiResponse<Void> response = ApiResponse.fail(CommonErrorCode.RESOURCE_NOT_FOUND);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getData()).isNull();
            assertThat(response.getError()).isNotNull();
            assertThat(response.getError().getCode()).isEqualTo(CommonErrorCode.RESOURCE_NOT_FOUND.getCode());
            assertThat(response.getError().getMessage()).isEqualTo(CommonErrorCode.RESOURCE_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("fail(ApiError)")
    class FailWithApiError {

        @Test
        @DisplayName("동적 메시지로 ApiError를 직접 넘길 수 있다")
        void fail_withApiError_setsCustomMessage() {
            ApiError apiError = ApiError.of("COMMON_001", "이름은 필수입니다.");
            ApiResponse<Void> response = ApiResponse.fail(apiError);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getError().getCode()).isEqualTo("COMMON_001");
            assertThat(response.getError().getMessage()).isEqualTo("이름은 필수입니다.");
        }
    }
}
