package org.howread.review.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.howread.shared.entity.BaseEntity;

/**
 * Rating 도메인 엔티티 - 별점 전용.
 *
 * 텍스트 리뷰(Review)와 완전히 분리된 별점 엔티티다.
 * 별점만 남기거나, 별점 없이 텍스트 리뷰만 남기거나, 둘 다 남길 수 있다.
 *
 * 1인 1책 1별점 정책. upsert 방식으로 재등록 시 기존 별점을 수정한다.
 */
@Entity
@Table(name = "ratings",
        indexes = {
                @Index(name = "idx_ratings_book_id", columnList = "book_id"),
                @Index(name = "idx_ratings_user_id", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_ratings_user_book", columnNames = {"user_id", "book_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rating extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    /** 별점 (1~5) */
    @Column(nullable = false)
    private int rating;

    public static Rating create(Long userId, Long bookId, int rating) {
        Rating r = new Rating();
        r.userId = userId;
        r.bookId = bookId;
        r.rating = rating;
        return r;
    }

    /**
     * 별점을 변경하고, 변경 전 값을 반환한다.
     * 호출자가 이벤트 발행 시 oldRating을 알 수 있도록 한다.
     */
    public int changeRating(int newRating) {
        int oldRating = this.rating;
        this.rating = newRating;
        return oldRating;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }
}
