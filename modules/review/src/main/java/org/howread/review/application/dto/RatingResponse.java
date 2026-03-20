package org.howread.review.application.dto;

import org.howread.review.domain.Rating;

import java.time.LocalDateTime;

public record RatingResponse(
        Long id,
        Long userId,
        Long bookId,
        int rating,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RatingResponse from(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getUserId(),
                rating.getBookId(),
                rating.getRating(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}
