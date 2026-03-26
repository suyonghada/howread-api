package org.howread.book.application.port;

/**
 * [Port] book 모듈이 review/rating 존재 여부를 조회하기 위한 계약.
 *
 * book 모듈이 review 모듈을 직접 참조하지 않도록 Port 패턴으로 역전한다.
 * 구현체는 infra 모듈의 UserContentExistencePortAdapter에 위치한다.
 */
public interface UserContentExistencePort {

    /** 해당 책에 작성된 리뷰(소프트 삭제 제외)가 하나라도 존재하면 true */
    boolean hasReviewForBook(Long bookId);

    /** 해당 책에 등록된 평점이 하나라도 존재하면 true */
    boolean hasRatingForBook(Long bookId);
}
