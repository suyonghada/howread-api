package org.howread.review.application.dto;

import org.howread.review.domain.Review;

import java.time.LocalDateTime;

/**
 * 텍스트 리뷰 응답 DTO.
 *
 * 별점(Rating)은 포함하지 않는다. 별점 정보가 필요하면 Rating API를 별도 호출한다.
 *
 * isBlurred가 true이면 비인증 사용자 요청으로, content는 null로 반환된다.
 */
public record ReviewResponse(
        Long id,
        Long userId,
        Long bookId,
        String content,
        int likeCount,
        boolean isLikedByMe,
        boolean isBlurred,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewResponse from(Review review, boolean isLikedByMe, boolean isBlurred) {
        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                review.getBookId(),
                isBlurred ? null : review.getContent(),
                review.getLikeCount(),
                isLikedByMe,
                isBlurred,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
