package org.howread.review.event;

import org.howread.common.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * 텍스트 리뷰가 새로 작성됐을 때 발행되는 도메인 이벤트.
 *
 * 별점(Rating)과 분리되어 Book 통계에 영향을 주지 않는다.
 */
public record ReviewCreatedEvent(
        Long reviewId,
        Long bookId,
        LocalDateTime occurredAt
) implements DomainEvent {

    public static ReviewCreatedEvent of(Long reviewId, Long bookId) {
        return new ReviewCreatedEvent(reviewId, bookId, LocalDateTime.now());
    }
}
