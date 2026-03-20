package org.howread.infra.review;

import lombok.RequiredArgsConstructor;
import org.howread.review.application.port.RatingRepository;
import org.howread.review.domain.Rating;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RatingRepositoryAdapter implements RatingRepository {

    private final RatingJpaRepository jpaRepository;

    @Override
    public Rating save(Rating rating) {
        return jpaRepository.save(rating);
    }

    @Override
    public Optional<Rating> findByUserIdAndBookId(Long userId, Long bookId) {
        return jpaRepository.findByUserIdAndBookId(userId, bookId);
    }

    @Override
    public void delete(Rating rating) {
        jpaRepository.delete(rating);
    }
}
