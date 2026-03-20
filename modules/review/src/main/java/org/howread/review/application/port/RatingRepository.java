package org.howread.review.application.port;

import org.howread.review.domain.Rating;

import java.util.Optional;

/**
 * Rating 도메인의 Repository Port.
 *
 * 인터페이스(Port)는 도메인 모듈에 정의하고, JPA 구현(Adapter)은 infra 모듈에 위치한다.
 */
public interface RatingRepository {

    Rating save(Rating rating);

    Optional<Rating> findByUserIdAndBookId(Long userId, Long bookId);

    void delete(Rating rating);
}
