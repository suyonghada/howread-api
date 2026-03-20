package org.howread.review.application;

import org.howread.common.exception.BusinessException;
import org.howread.review.application.dto.CreateReviewRequest;
import org.howread.review.application.dto.ReviewPageResponse;
import org.howread.review.application.dto.ReviewResponse;
import org.howread.review.application.dto.UpdateReviewRequest;
import org.howread.review.application.port.BookExistencePort;
import org.howread.review.application.port.ReviewLikeRepository;
import org.howread.review.application.port.ReviewRepository;
import org.howread.review.domain.Review;
import org.howread.review.domain.ReviewLike;
import org.howread.review.domain.ReviewSortType;
import org.howread.review.event.ReviewCreatedEvent;
import org.howread.review.event.ReviewDeletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    @Mock
    private BookExistencePort bookExistencePort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReviewService reviewService;

    private static Review createTestReview(Long id, Long userId, Long bookId) {
        Review review = Review.create(userId, bookId, "좋은 책입니다.");
        try {
            var field = Review.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(review, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return review;
    }

    private static ReviewLike createTestReviewLike(Long id, Long userId, Long reviewId) {
        ReviewLike like = ReviewLike.create(userId, reviewId);
        try {
            var field = ReviewLike.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(like, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return like;
    }

    @Nested
    @DisplayName("createReview()")
    class CreateReview {

        @Test
        @DisplayName("정상적으로 리뷰를 생성하고 ReviewCreatedEvent를 발행한다")
        void createReview_success() {
            Long userId = 1L, bookId = 10L;
            CreateReviewRequest request = new CreateReviewRequest("좋은 책입니다.");
            Review saved = createTestReview(100L, userId, bookId);

            given(bookExistencePort.existsById(bookId)).willReturn(true);
            given(reviewRepository.existsByUserIdAndBookId(userId, bookId)).willReturn(false);
            given(reviewRepository.save(any(Review.class))).willReturn(saved);

            ReviewResponse response = reviewService.createReview(userId, bookId, request);

            assertThat(response.id()).isEqualTo(100L);
            assertThat(response.isLikedByMe()).isFalse();
            assertThat(response.isBlurred()).isFalse();

            ArgumentCaptor<ReviewCreatedEvent> captor = ArgumentCaptor.forClass(ReviewCreatedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().bookId()).isEqualTo(bookId);
        }

        @Test
        @DisplayName("존재하지 않는 책이면 BOOK_NOT_FOUND 예외를 던진다")
        void createReview_bookNotFound_throws() {
            given(bookExistencePort.existsById(anyLong())).willReturn(false);

            assertThatThrownBy(() -> reviewService.createReview(1L, 999L,
                    new CreateReviewRequest("내용")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("존재하지 않는 책");
        }

        @Test
        @DisplayName("이미 리뷰를 작성한 책이면 REVIEW_002 예외를 던진다")
        void createReview_duplicate_throws() {
            given(bookExistencePort.existsById(anyLong())).willReturn(true);
            given(reviewRepository.existsByUserIdAndBookId(anyLong(), anyLong())).willReturn(true);

            assertThatThrownBy(() -> reviewService.createReview(1L, 10L,
                    new CreateReviewRequest("내용")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 해당 책에 리뷰를 작성하셨습니다");
        }
    }

    @Nested
    @DisplayName("updateReview()")
    class UpdateReview {

        @Test
        @DisplayName("정상적으로 리뷰 본문을 수정한다")
        void updateReview_success() {
            Long userId = 1L, reviewId = 100L;
            Review review = createTestReview(reviewId, userId, 10L);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
            given(reviewLikeRepository.existsByUserIdAndReviewId(userId, reviewId)).willReturn(false);

            ReviewResponse response = reviewService.updateReview(userId, reviewId,
                    new UpdateReviewRequest("수정된 내용"));

            assertThat(response.content()).isEqualTo("수정된 내용");
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("본인 리뷰가 아니면 REVIEW_003 예외를 던진다")
        void updateReview_notOwner_throws() {
            Review review = createTestReview(100L, 1L, 10L);
            given(reviewRepository.findById(100L)).willReturn(Optional.of(review));

            assertThatThrownBy(() -> reviewService.updateReview(2L, 100L,
                    new UpdateReviewRequest("내용")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("권한이 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 리뷰이면 REVIEW_001 예외를 던진다")
        void updateReview_notFound_throws() {
            given(reviewRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.updateReview(1L, 999L,
                    new UpdateReviewRequest("내용")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("리뷰를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("deleteReview()")
    class DeleteReview {

        @Test
        @DisplayName("정상적으로 삭제하고 ReviewDeletedEvent를 발행한다")
        void deleteReview_success() {
            Long userId = 1L, reviewId = 100L;
            Review review = createTestReview(reviewId, userId, 10L);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

            reviewService.deleteReview(userId, reviewId);

            verify(reviewRepository).delete(review);

            ArgumentCaptor<ReviewDeletedEvent> captor = ArgumentCaptor.forClass(ReviewDeletedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().bookId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("본인 리뷰가 아니면 REVIEW_003 예외를 던진다")
        void deleteReview_notOwner_throws() {
            Review review = createTestReview(100L, 1L, 10L);
            given(reviewRepository.findById(100L)).willReturn(Optional.of(review));

            assertThatThrownBy(() -> reviewService.deleteReview(2L, 100L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("권한이 없습니다");
        }
    }

    @Nested
    @DisplayName("likeReview()")
    class LikeReview {

        @Test
        @DisplayName("정상적으로 좋아요를 누르면 ReviewLike가 저장되고 likeCount가 증가한다")
        void likeReview_success() {
            Long userId = 1L, reviewId = 100L;
            Review review = createTestReview(reviewId, 2L, 10L);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
            given(reviewLikeRepository.existsByUserIdAndReviewId(userId, reviewId)).willReturn(false);
            given(reviewLikeRepository.save(any(ReviewLike.class)))
                    .willReturn(ReviewLike.create(userId, reviewId));

            reviewService.likeReview(userId, reviewId);

            verify(reviewLikeRepository).save(any(ReviewLike.class));
            assertThat(review.getLikeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("이미 좋아요를 눌렀으면 REVIEW_004 예외를 던진다")
        void likeReview_alreadyLiked_throws() {
            Long userId = 1L, reviewId = 100L;
            given(reviewRepository.findById(reviewId)).willReturn(
                    Optional.of(createTestReview(reviewId, 2L, 10L)));
            given(reviewLikeRepository.existsByUserIdAndReviewId(userId, reviewId)).willReturn(true);

            assertThatThrownBy(() -> reviewService.likeReview(userId, reviewId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 좋아요를 눌렀습니다");
        }
    }

    @Nested
    @DisplayName("unlikeReview()")
    class UnlikeReview {

        @Test
        @DisplayName("정상적으로 좋아요를 취소하면 ReviewLike가 삭제되고 likeCount가 감소한다")
        void unlikeReview_success() {
            Long userId = 1L, reviewId = 100L;
            Review review = createTestReview(reviewId, 2L, 10L);
            review.incrementLikeCount();

            ReviewLike like = createTestReviewLike(1L, userId, reviewId);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
            given(reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId))
                    .willReturn(Optional.of(like));

            reviewService.unlikeReview(userId, reviewId);

            verify(reviewLikeRepository).delete(like);
            assertThat(review.getLikeCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("좋아요를 누르지 않았으면 REVIEW_005 예외를 던진다")
        void unlikeReview_notLiked_throws() {
            Long userId = 1L, reviewId = 100L;
            given(reviewRepository.findById(reviewId)).willReturn(
                    Optional.of(createTestReview(reviewId, 2L, 10L)));
            given(reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.unlikeReview(userId, reviewId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("좋아요를 누르지 않은 리뷰");
        }
    }

    @Nested
    @DisplayName("getBookReviews()")
    class GetBookReviews {

        @Test
        @DisplayName("인증된 사용자는 좋아요한 리뷰에 isLikedByMe=true가 반영된다")
        void getBookReviews_authenticated_isLikedByMeSet() {
            Long userId = 1L, bookId = 10L;
            Review r1 = createTestReview(1L, 2L, bookId);
            Review r2 = createTestReview(2L, 3L, bookId);

            given(reviewRepository.findByBookId(bookId, ReviewSortType.LIKES_DESC, 0, 20))
                    .willReturn(List.of(r1, r2));
            given(reviewRepository.countByBookId(bookId)).willReturn(2L);
            given(reviewLikeRepository.findReviewIdsByUserIdAndReviewIdIn(userId, List.of(1L, 2L)))
                    .willReturn(Set.of(1L));

            ReviewPageResponse response = reviewService.getBookReviews(bookId, ReviewSortType.LIKES_DESC, 0, 20, userId);

            assertThat(response.data().get(0).isLikedByMe()).isTrue();
            assertThat(response.data().get(1).isLikedByMe()).isFalse();
            assertThat(response.data().get(0).isBlurred()).isFalse();
        }

        @Test
        @DisplayName("비인증 사용자는 isBlurred=true이고 content가 null이다")
        void getBookReviews_unauthenticated_isBlurred() {
            Long bookId = 10L;
            Review review = createTestReview(1L, 2L, bookId);

            given(reviewRepository.findByBookId(bookId, ReviewSortType.LIKES_DESC, 0, 20))
                    .willReturn(List.of(review));
            given(reviewRepository.countByBookId(bookId)).willReturn(1L);

            ReviewPageResponse response = reviewService.getBookReviews(bookId, ReviewSortType.LIKES_DESC, 0, 20, null);

            assertThat(response.data().get(0).isBlurred()).isTrue();
            assertThat(response.data().get(0).content()).isNull();
            assertThat(response.data().get(0).isLikedByMe()).isFalse();
            verifyNoInteractions(reviewLikeRepository);
        }
    }
}
