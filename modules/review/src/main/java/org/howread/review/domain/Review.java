package org.howread.review.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.howread.shared.entity.BaseEntity;

/**
 * Review 도메인 엔티티 (Rich Domain Model) - 텍스트 리뷰 전용.
 *
 * 별점(Rating)과 완전히 분리된 구조다.
 * 별점 없이 텍스트 리뷰만 남길 수 있고, 텍스트 리뷰 없이 별점만 남길 수도 있다.
 *
 * 1인 1책 1리뷰 정책을 DB 레벨 unique 제약으로 보장한다.
 * likeCount를 비정규화하여 좋아요 기준 정렬 시 COUNT JOIN을 방지한다.
 */
@Entity
@Table(name = "reviews",
        indexes = {
                @Index(name = "idx_reviews_book_id", columnList = "book_id"),
                @Index(name = "idx_reviews_user_id", columnList = "user_id"),
                @Index(name = "idx_reviews_like_count_id", columnList = "like_count, id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_reviews_user_book", columnNames = {"user_id", "book_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    /**
     * 텍스트 리뷰 생성 팩토리 메서드.
     */
    public static Review create(Long userId, Long bookId, String content) {
        Review review = new Review();
        review.userId = userId;
        review.bookId = bookId;
        review.content = content;
        review.likeCount = 0;
        return review;
    }

    public void update(String content) {
        this.content = content;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount <= 0) {
            throw new IllegalStateException("likeCount cannot be negative");
        }
        this.likeCount--;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }
}
