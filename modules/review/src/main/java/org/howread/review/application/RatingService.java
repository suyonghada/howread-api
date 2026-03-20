package org.howread.review.application;

import lombok.RequiredArgsConstructor;
import org.howread.common.exception.BusinessException;
import org.howread.review.application.dto.CreateRatingRequest;
import org.howread.review.application.dto.RatingResponse;
import org.howread.review.application.port.BookExistencePort;
import org.howread.review.application.port.RatingRepository;
import org.howread.review.domain.Rating;
import org.howread.review.event.RatingChangedEvent;
import org.howread.review.event.RatingCreatedEvent;
import org.howread.review.event.RatingDeletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 별점 도메인의 Application Service.
 *
 * upsert 방식: 해당 책에 별점이 이미 존재하면 수정, 없으면 생성한다.
 *
 * 도메인 이벤트(RatingCreatedEvent 등)를 발행하여 Book 통계를 갱신한다.
 * 이벤트 리스너는 @Async + @TransactionalEventListener(AFTER_COMMIT)로 비동기 처리되므로,
 * 별점 트랜잭션 커밋 후 별도 스레드에서 Book 통계가 갱신된다 (Eventually Consistent).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingService {

    private final RatingRepository ratingRepository;
    private final BookExistencePort bookExistencePort;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 별점을 등록하거나 수정한다 (upsert).
     *
     * 이미 별점이 있으면 수정하고 RatingChangedEvent를 발행한다.
     * 없으면 새로 생성하고 RatingCreatedEvent를 발행한다.
     */
    @Transactional
    public RatingResponse upsertRating(Long userId, Long bookId, CreateRatingRequest request) {
        if (!bookExistencePort.existsById(bookId)) {
            throw new BusinessException(ReviewErrorCode.BOOK_NOT_FOUND);
        }

        Optional<Rating> existing = ratingRepository.findByUserIdAndBookId(userId, bookId);

        if (existing.isPresent()) {
            Rating rating = existing.get();
            int oldRating = rating.changeRating(request.rating());
            eventPublisher.publishEvent(
                    RatingChangedEvent.of(rating.getId(), bookId, oldRating, request.rating()));
            return RatingResponse.from(rating);
        }

        Rating rating = Rating.create(userId, bookId, request.rating());
        Rating saved = ratingRepository.save(rating);
        eventPublisher.publishEvent(RatingCreatedEvent.of(saved.getId(), bookId, request.rating()));
        return RatingResponse.from(saved);
    }

    /**
     * 별점을 삭제한다.
     */
    @Transactional
    public void deleteRating(Long userId, Long bookId) {
        Rating rating = ratingRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new BusinessException(ReviewErrorCode.RATING_NOT_FOUND));

        int ratingValue = rating.getRating();
        Long ratingId = rating.getId();

        ratingRepository.delete(rating);
        eventPublisher.publishEvent(RatingDeletedEvent.of(ratingId, bookId, ratingValue));
    }

    /**
     * 내 별점을 조회한다.
     */
    public Optional<RatingResponse> getMyRating(Long userId, Long bookId) {
        return ratingRepository.findByUserIdAndBookId(userId, bookId)
                .map(RatingResponse::from);
    }
}
