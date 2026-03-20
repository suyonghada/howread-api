package org.howread.review.event;

import org.howread.common.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * 별점이 변경됐을 때 발행되는 도메인 이벤트.
 *
 * Book 도메인의 averageRating 재계산에 활용된다.
 */
public record RatingChangedEvent(
        Long ratingId,
        Long bookId,
        int oldRating,
        int newRating,
        LocalDateTime occurredAt
) implements DomainEvent {

    public static RatingChangedEvent of(Long ratingId, Long bookId, int oldRating, int newRating) {
        return new RatingChangedEvent(ratingId, bookId, oldRating, newRating, LocalDateTime.now());
    }
}
