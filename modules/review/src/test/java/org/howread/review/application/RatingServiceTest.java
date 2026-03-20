package org.howread.review.application;

import org.howread.common.exception.BusinessException;
import org.howread.review.application.dto.CreateRatingRequest;
import org.howread.review.application.dto.RatingResponse;
import org.howread.review.application.port.BookExistencePort;
import org.howread.review.application.port.RatingRepository;
import org.howread.review.domain.Rating;
import org.howread.review.event.RatingChangedEvent;
import org.howread.review.event.RatingCreatedEvent;
import org.howread.review.event.RatingDeletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private BookExistencePort bookExistencePort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RatingService ratingService;

    private static Rating createTestRating(Long id, Long userId, Long bookId, int rating) {
        Rating r = Rating.create(userId, bookId, rating);
        try {
            var field = Rating.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(r, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return r;
    }

    @Nested
    @DisplayName("upsertRating() - 신규 등록")
    class UpsertRatingCreate {

        @Test
        @DisplayName("별점이 없으면 새로 생성하고 RatingCreatedEvent를 발행한다")
        void upsertRating_create_success() {
            Long userId = 1L, bookId = 10L;
            CreateRatingRequest request = new CreateRatingRequest(4);
            Rating saved = createTestRating(100L, userId, bookId, 4);

            given(bookExistencePort.existsById(bookId)).willReturn(true);
            given(ratingRepository.findByUserIdAndBookId(userId, bookId)).willReturn(Optional.empty());
            given(ratingRepository.save(any(Rating.class))).willReturn(saved);

            RatingResponse response = ratingService.upsertRating(userId, bookId, request);

            assertThat(response.rating()).isEqualTo(4);
            assertThat(response.id()).isEqualTo(100L);

            ArgumentCaptor<RatingCreatedEvent> captor = ArgumentCaptor.forClass(RatingCreatedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().bookId()).isEqualTo(bookId);
            assertThat(captor.getValue().rating()).isEqualTo(4);
        }

        @Test
        @DisplayName("존재하지 않는 책이면 BOOK_NOT_FOUND 예외를 던진다")
        void upsertRating_bookNotFound_throws() {
            given(bookExistencePort.existsById(anyLong())).willReturn(false);

            assertThatThrownBy(() -> ratingService.upsertRating(1L, 999L, new CreateRatingRequest(4)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("존재하지 않는 책");
        }
    }

    @Nested
    @DisplayName("upsertRating() - 수정")
    class UpsertRatingUpdate {

        @Test
        @DisplayName("이미 별점이 있으면 수정하고 RatingChangedEvent를 발행한다")
        void upsertRating_update_success() {
            Long userId = 1L, bookId = 10L;
            Rating existing = createTestRating(100L, userId, bookId, 3);
            CreateRatingRequest request = new CreateRatingRequest(5);

            given(bookExistencePort.existsById(bookId)).willReturn(true);
            given(ratingRepository.findByUserIdAndBookId(userId, bookId)).willReturn(Optional.of(existing));

            RatingResponse response = ratingService.upsertRating(userId, bookId, request);

            assertThat(response.rating()).isEqualTo(5);

            ArgumentCaptor<RatingChangedEvent> captor = ArgumentCaptor.forClass(RatingChangedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().bookId()).isEqualTo(bookId);
            assertThat(captor.getValue().oldRating()).isEqualTo(3);
            assertThat(captor.getValue().newRating()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("deleteRating()")
    class DeleteRating {

        @Test
        @DisplayName("정상적으로 삭제하고 RatingDeletedEvent를 발행한다")
        void deleteRating_success() {
            Long userId = 1L, bookId = 10L;
            Rating rating = createTestRating(100L, userId, bookId, 4);

            given(ratingRepository.findByUserIdAndBookId(userId, bookId)).willReturn(Optional.of(rating));

            ratingService.deleteRating(userId, bookId);

            verify(ratingRepository).delete(rating);

            ArgumentCaptor<RatingDeletedEvent> captor = ArgumentCaptor.forClass(RatingDeletedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().bookId()).isEqualTo(bookId);
            assertThat(captor.getValue().rating()).isEqualTo(4);
        }

        @Test
        @DisplayName("별점이 없으면 RATING_NOT_FOUND 예외를 던진다")
        void deleteRating_notFound_throws() {
            given(ratingRepository.findByUserIdAndBookId(anyLong(), anyLong())).willReturn(Optional.empty());

            assertThatThrownBy(() -> ratingService.deleteRating(1L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("별점을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("getMyRating()")
    class GetMyRating {

        @Test
        @DisplayName("별점이 있으면 RatingResponse를 반환한다")
        void getMyRating_exists_returnsResponse() {
            Long userId = 1L, bookId = 10L;
            Rating rating = createTestRating(100L, userId, bookId, 3);

            given(ratingRepository.findByUserIdAndBookId(userId, bookId)).willReturn(Optional.of(rating));

            var result = ratingService.getMyRating(userId, bookId);

            assertThat(result).isPresent();
            assertThat(result.get().rating()).isEqualTo(3);
        }

        @Test
        @DisplayName("별점이 없으면 Optional.empty()를 반환한다")
        void getMyRating_notExists_returnsEmpty() {
            given(ratingRepository.findByUserIdAndBookId(anyLong(), anyLong())).willReturn(Optional.empty());

            var result = ratingService.getMyRating(1L, 10L);

            assertThat(result).isEmpty();
        }
    }
}
