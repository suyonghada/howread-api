package org.howread.infra.review;

import org.howread.review.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RatingJpaRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndBookId(Long userId, Long bookId);

    long countByBookId(Long bookId);
}
