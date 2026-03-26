package org.howread.infra.book;

import lombok.RequiredArgsConstructor;
import org.howread.book.application.port.UserContentExistencePort;
import org.howread.infra.review.RatingJpaRepository;
import org.howread.infra.review.ReviewJpaRepository;
import org.springframework.stereotype.Component;

/**
 * [Adapter] UserContentExistencePort 구현체.
 *
 * book 모듈이 review/rating 모듈을 직접 참조하지 않도록
 * infra 계층에서 두 JPA 레포지토리를 통해 존재 여부를 조회한다.
 */
@Component
@RequiredArgsConstructor
public class UserContentExistencePortAdapter implements UserContentExistencePort {

    private final ReviewJpaRepository reviewJpaRepository;
    private final RatingJpaRepository ratingJpaRepository;

    @Override
    public boolean hasReviewForBook(Long bookId) {
        return reviewJpaRepository.countByBookId(bookId) > 0;
    }

    @Override
    public boolean hasRatingForBook(Long bookId) {
        return ratingJpaRepository.countByBookId(bookId) > 0;
    }
}
