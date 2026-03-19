package org.howread.book.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.howread.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BookErrorCode implements ErrorCode {

    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_001", "책을 찾을 수 없습니다."),
    BOOK_ALREADY_EXISTS(HttpStatus.CONFLICT, "BOOK_002", "이미 등록된 책입니다."),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "BOOK_003", "외부 도서 API 호출에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
