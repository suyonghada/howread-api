package org.howread.review.event;

import org.howread.common.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * 별점이 삭제됐을 때 발행되는 도메인 이벤트.
 *
 * Book 도메인의 averageRating, ratingCount 갱신에 활용된다.
 */
public record RatingDeletedEvent(
        Long ratingId,
        Long bookId,
        int rating,
        LocalDateTime occurredAt
) implements DomainEvent {

    public static RatingDeletedEvent of(Long ratingId, Long bookId, int rating) {
        return new RatingDeletedEvent(ratingId, bookId, rating, LocalDateTime.now());
    }
}
