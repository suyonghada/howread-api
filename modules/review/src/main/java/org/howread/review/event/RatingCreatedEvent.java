package org.howread.review.event;

import org.howread.common.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * 별점이 새로 등록됐을 때 발행되는 도메인 이벤트.
 *
 * Book 도메인의 averageRating, ratingCount 갱신에 활용된다.
 */
public record RatingCreatedEvent(
        Long ratingId,
        Long bookId,
        int rating,
        LocalDateTime occurredAt
) implements DomainEvent {

    public static RatingCreatedEvent of(Long ratingId, Long bookId, int rating) {
        return new RatingCreatedEvent(ratingId, bookId, rating, LocalDateTime.now());
    }
}
