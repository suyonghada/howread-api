package org.howread.review.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.howread.shared.entity.BaseEntity;

/**
 * ReviewLike 도메인 엔티티.
 *
 * 사용자와 리뷰 간의 좋아요 관계를 표현한다.
 * (userId, reviewId) unique 제약으로 중복 좋아요를 DB 레벨에서 방어한다.
 */
@Entity
@Table(name = "review_likes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_review_likes_user_review", columnNames = {"user_id", "review_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    public static ReviewLike create(Long userId, Long reviewId) {
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.userId = userId;
        reviewLike.reviewId = reviewId;
        return reviewLike;
    }
}
