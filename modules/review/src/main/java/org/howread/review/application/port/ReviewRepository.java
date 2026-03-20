package org.howread.review.application.port;

import org.howread.review.domain.Review;
import org.howread.review.domain.ReviewSortType;

import java.util.List;
import java.util.Optional;

/**
 * Review 도메인의 Repository Port.
 *
 * 인터페이스(Port)는 도메인 모듈에 정의하고, JPA 구현(Adapter)은 infra 모듈에 위치한다.
 * 이를 통해 도메인이 인프라에 오염되지 않는다.
 */
public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(Long id);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    void delete(Review review);

    /**
     * 책 ID로 리뷰 목록을 정렬 기준과 함께 offset 페이지네이션으로 조회한다.
     *
     * @param page 0-based 페이지 번호
     * @param size 페이지 크기
     */
    List<Review> findByBookId(Long bookId, ReviewSortType sortType, int page, int size);

    long countByBookId(Long bookId);

    /**
     * 사용자 ID로 내가 쓴 리뷰 목록을 최신 순으로 조회한다.
     */
    List<Review> findByUserId(Long userId, int page, int size);

    long countByUserId(Long userId);
}
