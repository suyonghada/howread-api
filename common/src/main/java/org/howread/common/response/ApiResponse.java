package org.howread.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.howread.common.exception.ErrorCode;

/**
 * 모든 API 엔드포인트의 공통 응답 래퍼.
 *
 * 성공: { "success": true,  "data": {...}, "error": null }
 * 실패: { "success": false, "data": null,  "error": { "code": "...", "message": "..." } }
 *
 * 정적 팩토리 메서드 패턴을 사용하여:
 * 1. 생성자 대신 의미 있는 이름(success, fail)으로 의도를 명확히 한다.
 * 2. 성공/실패 분기를 호출 시점에서 직관적으로 구분한다.
 *
 * 제네릭 <T>를 사용하여 각 API의 응답 데이터 타입을 컴파일 타임에 검사한다.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ApiError error;

    /**
     * 데이터를 포함한 성공 응답.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 데이터 없는 성공 응답. (201 Created, 204 No Content 등)
     */
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null);
    }

    /**
     * ErrorCode 기반 에러 응답. GlobalExceptionHandler에서 사용한다.
     */
    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, ApiError.of(errorCode));
    }

    /**
     * ApiError 기반 에러 응답.
     * @Valid 검증 실패처럼 고정 메시지가 아닌 동적 메시지가 필요할 때 사용한다.
     */
    public static <T> ApiResponse<T> fail(ApiError apiError) {
        return new ApiResponse<>(false, null, apiError);
    }
}
