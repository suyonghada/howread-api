package org.howread.review.application;

import lombok.RequiredArgsConstructor;
import org.howread.common.exception.BusinessException;
import org.howread.review.application.dto.CreateReviewRequest;
import org.howread.review.application.dto.ReviewPageResponse;
import org.howread.review.application.dto.ReviewResponse;
import org.howread.review.application.dto.UpdateReviewRequest;
import org.howread.review.application.dto.UserSummary;
import org.howread.review.application.port.BookExistencePort;
import org.howread.review.application.port.ReviewLikeRepository;
import org.howread.review.application.port.ReviewRepository;
import org.howread.review.application.port.UserInfoPort;
import org.howread.review.domain.Review;
import org.howread.review.domain.ReviewLike;
import org.howread.review.domain.ReviewSortType;
import org.howread.review.event.ReviewCreatedEvent;
import org.howread.review.event.ReviewDeletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 텍스트 리뷰 도메인의 Application Service.
 *
 * 별점(Rating) 관련 로직은 RatingService에서 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final BookExistencePort bookExistencePort;
    private final UserInfoPort userInfoPort;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 텍스트 리뷰를 작성한다.
     */
    @Transactional
    public ReviewResponse createReview(Long userId, Long bookId, CreateReviewRequest request) {
        if (!bookExistencePort.existsById(bookId)) {
            throw new BusinessException(ReviewErrorCode.BOOK_NOT_FOUND);
        }
        if (reviewRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new BusinessException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.create(userId, bookId, request.content());
        Review saved = reviewRepository.save(review);

        eventPublisher.publishEvent(ReviewCreatedEvent.of(saved.getId(), bookId));

        UserSummary author = userInfoPort.findSummariesByIds(List.of(userId)).get(userId);
        return ReviewResponse.from(saved, author, false, true, false);
    }

    /**
     * 텍스트 리뷰를 수정한다.
     */
    @Transactional
    public ReviewResponse updateReview(Long userId, Long reviewId, UpdateReviewRequest request) {
        Review review = findReviewOrThrow(reviewId);
        validateOwnership(review, userId);

        review.update(request.content());

        boolean isLikedByMe = reviewLikeRepository.existsByUserIdAndReviewId(userId, reviewId);
        UserSummary author = userInfoPort.findSummariesByIds(List.of(userId)).get(userId);
        return ReviewResponse.from(review, author, isLikedByMe, true, false);
    }

    /**
     * 텍스트 리뷰를 삭제한다.
     */
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = findReviewOrThrow(reviewId);
        validateOwnership(review, userId);

        Long bookId = review.getBookId();
        reviewRepository.delete(review);

        eventPublisher.publishEvent(ReviewDeletedEvent.of(reviewId, bookId));
    }

    /**
     * 리뷰에 좋아요를 누른다.
     */
    @Transactional
    public void likeReview(Long userId, Long reviewId) {
        Review review = findReviewOrThrow(reviewId);

        if (reviewLikeRepository.existsByUserIdAndReviewId(userId, reviewId)) {
            throw new BusinessException(ReviewErrorCode.REVIEW_LIKE_ALREADY_EXISTS);
        }

        reviewLikeRepository.save(ReviewLike.create(userId, reviewId));
        review.incrementLikeCount();
    }

    /**
     * 리뷰 좋아요를 취소한다.
     */
    @Transactional
    public void unlikeReview(Long userId, Long reviewId) {
        Review review = findReviewOrThrow(reviewId);

        ReviewLike reviewLike = reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId)
                .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_LIKE_NOT_FOUND));

        reviewLikeRepository.delete(reviewLike);
        review.decrementLikeCount();
    }

    /**
     * 책의 텍스트 리뷰 목록을 조회한다.
     *
     * 비인증 사용자(currentUserId=null)에게는 content/nickname=null, isBlurred=true로 응답한다.
     */
    public ReviewPageResponse getBookReviews(Long bookId, ReviewSortType sortType,
                                              int page, int size, Long currentUserId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId, sortType, page, size);
        long totalCount = reviewRepository.countByBookId(bookId);

        boolean isAuthenticated = currentUserId != null;
        List<Long> reviewIds = reviews.stream().map(Review::getId).toList();
        List<Long> authorIds = reviews.stream().map(Review::getUserId).distinct().toList();

        Set<Long> likedIds = isAuthenticated && !reviewIds.isEmpty()
                ? reviewLikeRepository.findReviewIdsByUserIdAndReviewIdIn(currentUserId, reviewIds)
                : Set.of();

        Map<Long, UserSummary> authorMap = !authorIds.isEmpty()
                ? userInfoPort.findSummariesByIds(authorIds)
                : Map.of();

        List<ReviewResponse> responses = reviews.stream()
                .map(r -> ReviewResponse.from(
                        r,
                        authorMap.get(r.getUserId()),
                        likedIds.contains(r.getId()),
                        isAuthenticated && r.getUserId().equals(currentUserId),
                        !isAuthenticated
                ))
                .toList();

        return ReviewPageResponse.of(responses, page, size, totalCount);
    }

    /**
     * 내가 쓴 텍스트 리뷰 목록을 조회한다.
     */
    public ReviewPageResponse getMyReviews(Long userId, int page, int size) {
        List<Review> reviews = reviewRepository.findByUserId(userId, page, size);
        long totalCount = reviewRepository.countByUserId(userId);

        List<Long> reviewIds = reviews.stream().map(Review::getId).toList();
        Set<Long> likedIds = !reviewIds.isEmpty()
                ? reviewLikeRepository.findReviewIdsByUserIdAndReviewIdIn(userId, reviewIds)
                : Set.of();

        UserSummary author = userInfoPort.findSummariesByIds(List.of(userId)).get(userId);

        List<ReviewResponse> responses = reviews.stream()
                .map(r -> ReviewResponse.from(r, author, likedIds.contains(r.getId()), true, false))
                .toList();

        return ReviewPageResponse.of(responses, page, size, totalCount);
    }

    private Review findReviewOrThrow(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    private void validateOwnership(Review review, Long userId) {
        if (!review.isOwnedBy(userId)) {
            throw new BusinessException(ReviewErrorCode.REVIEW_FORBIDDEN);
        }
    }
}
