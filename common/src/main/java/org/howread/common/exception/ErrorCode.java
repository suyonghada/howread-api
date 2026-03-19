package org.howread.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 도메인별 에러 코드 계약(Contract) 인터페이스.
 *
 * 각 도메인 모듈은 이 인터페이스를 구현하는 enum을 정의한다.
 * 예: BookErrorCode implements ErrorCode
 *     ReviewErrorCode implements ErrorCode
 *
 * interface로 설계한 이유:
 * - enum은 상속 불가. 공통 에러(CommonErrorCode)와 도메인 에러(BookErrorCode)를
 *   동일한 타입 계층으로 묶으려면 공통 인터페이스가 필수.
 * - GlobalExceptionHandler가 ErrorCode 타입 하나만 알면 되므로
 *   도메인 모듈이 추가되어도 핸들러 코드를 수정하지 않아도 된다. (OCP)
 */
public interface ErrorCode {

    HttpStatus getHttpStatus();

    /**
     * 클라이언트가 에러를 식별하는 문자열 코드.
     * HTTP 상태 코드만으로는 에러의 의미를 충분히 전달하지 못하므로 별도 코드를 부여한다.
     * 예: "BOOK_001", "USER_001", "COMMON_002"
     */
    String getCode();

    String getMessage();
}
