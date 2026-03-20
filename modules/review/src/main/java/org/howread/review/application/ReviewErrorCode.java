package org.howread.review.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.howread.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    // 텍스트 리뷰 관련
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_001", "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW_002", "이미 해당 책에 리뷰를 작성하셨습니다."),
    REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "REVIEW_003", "리뷰를 수정/삭제할 권한이 없습니다."),
    REVIEW_LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW_004", "이미 좋아요를 눌렀습니다."),
    REVIEW_LIKE_NOT_FOUND(HttpStatus.BAD_REQUEST, "REVIEW_005", "좋아요를 누르지 않은 리뷰입니다."),

    // 별점 관련
    RATING_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_006", "별점을 찾을 수 없습니다."),

    // 공통
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_007", "존재하지 않는 책입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
