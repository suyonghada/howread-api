package org.howread.user.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.howread.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * User 도메인 전용 에러 코드.
 *
 * ErrorCode 인터페이스를 구현하여 GlobalExceptionHandler가
 * 이 타입을 직접 알 필요 없이 ErrorCode 타입만으로 처리한다. (OCP)
 */
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_001", "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 닉네임입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "USER_003", "이메일 인증이 완료되지 않았습니다."),
    VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, "USER_004", "인증번호가 올바르지 않습니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "USER_005", "인증번호가 만료되었습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_006", "사용자를 찾을 수 없습니다."),
    PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "USER_007", "현재 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "USER_008", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "USER_009", "토큰이 만료되었습니다."),
    DELETED_ACCOUNT(HttpStatus.FORBIDDEN, "USER_010", "탈퇴한 계정입니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "USER_011", "지원하지 않는 파일 형식입니다. (jpeg, png, webp)"),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "USER_012", "파일 크기는 10MB 이하여야 합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
