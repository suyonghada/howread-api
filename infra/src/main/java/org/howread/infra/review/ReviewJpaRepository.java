package org.howread.infra.review;

import org.howread.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    Optional<Review> findByUserIdAndBookId(Long userId, Long bookId);

    long countByBookId(Long bookId);

    long countByUserId(Long userId);
}
