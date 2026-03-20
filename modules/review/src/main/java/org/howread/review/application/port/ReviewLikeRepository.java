package org.howread.review.application.port;

import org.howread.review.domain.ReviewLike;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * ReviewLike 도메인의 Repository Port.
 */
public interface ReviewLikeRepository {

    ReviewLike save(ReviewLike reviewLike);

    Optional<ReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId);

    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);

    void delete(ReviewLike reviewLike);

    /**
     * 주어진 리뷰 ID 목록 중 해당 사용자가 좋아요를 누른 리뷰 ID의 집합을 반환한다.
     * N+1 없이 배치로 isLikedByMe 여부를 계산하기 위해 사용한다.
     */
    Set<Long> findReviewIdsByUserIdAndReviewIdIn(Long userId, List<Long> reviewIds);
}
