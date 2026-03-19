package org.howread.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.howread.common.exception.ErrorCode;

/**
 * 에러 응답의 상세 정보를 담는 불변 VO.
 *
 * ApiResponse의 error 필드에 포함된다.
 * code는 클라이언트가 에러 유형을 식별하는 데 사용하고,
 * message는 사용자에게 노출 가능한 메시지를 제공한다.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiError {

    private final String code;
    private final String message;

    public static ApiError of(ErrorCode errorCode) {
        return new ApiError(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * @Valid 검증 실패 시 필드별 커스텀 메시지를 담을 때 사용.
     */
    public static ApiError of(String code, String message) {
        return new ApiError(code, message);
    }
}
