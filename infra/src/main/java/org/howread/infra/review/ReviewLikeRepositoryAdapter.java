package org.howread.infra.review;

import lombok.RequiredArgsConstructor;
import org.howread.review.application.port.ReviewLikeRepository;
import org.howread.review.domain.ReviewLike;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ReviewLikeRepositoryAdapter implements ReviewLikeRepository {

    private final ReviewLikeJpaRepository jpaRepository;

    @Override
    public ReviewLike save(ReviewLike reviewLike) {
        return jpaRepository.save(reviewLike);
    }

    @Override
    public Optional<ReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId) {
        return jpaRepository.findByUserIdAndReviewId(userId, reviewId);
    }

    @Override
    public boolean existsByUserIdAndReviewId(Long userId, Long reviewId) {
        return jpaRepository.existsByUserIdAndReviewId(userId, reviewId);
    }

    @Override
    public void delete(ReviewLike reviewLike) {
        jpaRepository.delete(reviewLike);
    }

    @Override
    public Set<Long> findReviewIdsByUserIdAndReviewIdIn(Long userId, List<Long> reviewIds) {
        // HashSet으로 변환하여 contains() 호출이 O(1)이 되도록 한다
        return new HashSet<>(jpaRepository.findReviewIdsByUserIdAndReviewIdIn(userId, reviewIds));
    }
}
