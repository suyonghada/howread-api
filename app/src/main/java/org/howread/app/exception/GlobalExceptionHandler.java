package org.howread.app.exception;

import lombok.extern.slf4j.Slf4j;
import org.howread.common.exception.BusinessException;
import org.howread.common.exception.CommonErrorCode;
import org.howread.common.response.ApiError;
import org.howread.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 핸들러.
 *
 * app 모듈에 위치하는 이유:
 * - @RestControllerAdvice, ResponseEntity는 web 관심사이므로
 *   common 모듈이 spring-webmvc에 의존하게 되는 오염을 방지한다.
 * - Controller도 app 모듈에 위치하므로 동일 컨텍스트에서 스캔된다.
 *
 * 처리 우선순위:
 * 1. BusinessException — 도메인에서 의도적으로 던진 예외
 * 2. MethodArgumentNotValidException — @Valid 검증 실패
 * 3. Exception — 예상치 못한 모든 예외 (fallback)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 도메인 비즈니스 규칙 위반 예외 처리.
     * ErrorCode가 HTTP 상태 코드와 메시지를 모두 가지고 있으므로
     * 핸들러는 단순히 위임만 한다. (OCP 준수)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("비즈니스 예외 발생: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.fail(e.getErrorCode()));
    }

    /**
     * @Valid, @Validated 검증 실패 처리.
     *
     * 첫 번째 필드 에러만 반환하는 이유:
     * - 여러 필드 에러를 모두 반환하면 ApiResponse 구조가 복잡해진다.
     * - UX 관점에서 사용자는 한 번에 하나의 에러를 수정하는 것이 더 자연스럽다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);
        ApiError apiError = ApiError.of(CommonErrorCode.INVALID_INPUT_VALUE.getCode(), fieldError.getDefaultMessage());

        log.warn("입력값 검증 실패: 필드={}, 메시지={}", fieldError.getField(), fieldError.getDefaultMessage());

        return ResponseEntity
                .status(CommonErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
                .body(ApiResponse.fail(apiError));
    }

    /**
     * 예상치 못한 예외의 fallback 핸들러.
     * 내부 구현 세부사항이 클라이언트에 노출되지 않도록 INTERNAL_SERVER_ERROR로 감싼다.
     * 스택 트레이스는 로그에만 기록한다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("처리되지 않은 예외 발생", e);
        return ResponseEntity
                .status(CommonErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.fail(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }
}
