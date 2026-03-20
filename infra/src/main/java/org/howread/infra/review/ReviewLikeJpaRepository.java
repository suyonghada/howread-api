package org.howread.infra.review;

import org.howread.review.domain.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewLikeJpaRepository extends JpaRepository<ReviewLike, Long> {

    Optional<ReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId);

    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);

    /**
     * 주어진 리뷰 ID 목록 중 해당 사용자가 좋아요를 누른 reviewId만 조회한다.
     * IN 쿼리로 배치 처리하여 N+1 문제를 방지한다.
     */
    @Query("SELECT rl.reviewId FROM ReviewLike rl WHERE rl.userId = :userId AND rl.reviewId IN :reviewIds")
    List<Long> findReviewIdsByUserIdAndReviewIdIn(
            @Param("userId") Long userId,
            @Param("reviewIds") List<Long> reviewIds
    );
}
