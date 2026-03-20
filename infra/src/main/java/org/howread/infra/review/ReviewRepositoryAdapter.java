package org.howread.infra.review;

import lombok.RequiredArgsConstructor;
import org.howread.review.application.port.ReviewRepository;
import org.howread.review.domain.Review;
import org.howread.review.domain.ReviewSortType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepository {

    private final ReviewJpaRepository jpaRepository;
    private final ReviewQueryRepository queryRepository;

    @Override
    public Review save(Review review) {
        return jpaRepository.save(review);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public boolean existsByUserIdAndBookId(Long userId, Long bookId) {
        return jpaRepository.existsByUserIdAndBookId(userId, bookId);
    }

    @Override
    public void delete(Review review) {
        jpaRepository.delete(review);
    }

    @Override
    public List<Review> findByBookId(Long bookId, ReviewSortType sortType, int page, int size) {
        return queryRepository.findByBookId(bookId, sortType, page, size);
    }

    @Override
    public long countByBookId(Long bookId) {
        return jpaRepository.countByBookId(bookId);
    }

    @Override
    public List<Review> findByUserId(Long userId, int page, int size) {
        return queryRepository.findByUserId(userId, page, size);
    }

    @Override
    public long countByUserId(Long userId) {
        return jpaRepository.countByUserId(userId);
    }
}
