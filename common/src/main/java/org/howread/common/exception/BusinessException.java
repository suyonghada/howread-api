package org.howread.common.exception;

import lombok.Getter;

/**
 * 비즈니스 규칙 위반 시 발생하는 예외의 최상위 클래스.
 *
 * RuntimeException을 상속하여 트랜잭션 롤백이 자동으로 적용된다.
 * ErrorCode를 필드로 가짐으로써 GlobalExceptionHandler가
 * HTTP 상태 코드·메시지를 ErrorCode에서 직접 읽을 수 있다.
 *
 * 사용 예:
 *   throw new BusinessException(BookErrorCode.BOOK_NOT_FOUND);
 *   throw new BusinessException(BookErrorCode.BOOK_NOT_FOUND, cause);
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 원인 예외(cause)를 함께 전달하는 생성자.
     * 예외 연쇄(exception chaining)를 유지하여 디버깅 시 루트 원인을 추적할 수 있다.
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
