package org.howread.review.application.dto;

import org.howread.review.domain.Review;

import java.time.LocalDateTime;

/**
 * 텍스트 리뷰 응답 DTO.
 *
 * isBlurred가 true이면 비인증 사용자 요청으로, content/nickname/profileImageUrl은 null로 반환된다.
 * isOwner가 true이면 현재 로그인 사용자가 작성한 리뷰로, 수정/삭제 권한이 있다.
 */
public record ReviewResponse(
        Long id,
        Long userId,
        String nickname,
        String profileImageUrl,
        Long bookId,
        String content,
        int likeCount,
        boolean isLikedByMe,
        boolean isOwner,
        boolean isBlurred,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewResponse from(Review review, UserSummary author,
                                      boolean isLikedByMe, boolean isOwner, boolean isBlurred) {
        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                isBlurred ? null : (author != null ? author.nickname() : null),
                isBlurred ? null : (author != null ? author.profileImageUrl() : null),
                review.getBookId(),
                isBlurred ? null : review.getContent(),
                review.getLikeCount(),
                isLikedByMe,
                isOwner,
                isBlurred,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
