package org.howread.infra.review;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.howread.review.domain.QReview;
import org.howread.review.domain.Review;
import org.howread.review.domain.ReviewSortType;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * QueryDSL 기반 Review 검색 레포지토리.
 *
 * ReviewSortType에 따라 정렬 기준을 동적으로 적용한다.
 * Java의 exhaustive switch expression을 사용하여 새 ReviewSortType 추가 시 컴파일 오류로 누락을 감지한다.
 */
@Repository
@RequiredArgsConstructor
public class ReviewQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final QReview review = QReview.review;

    public List<Review> findByBookId(Long bookId, ReviewSortType sortType, int page, int size) {
        return queryFactory
                .selectFrom(review)
                .where(review.bookId.eq(bookId))
                .orderBy(buildOrderSpecifier(sortType))
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    public List<Review> findByUserId(Long userId, int page, int size) {
        return queryFactory
                .selectFrom(review)
                .where(review.userId.eq(userId))
                .orderBy(review.id.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    private OrderSpecifier<?>[] buildOrderSpecifier(ReviewSortType sortType) {
        return switch (sortType) {
            case LIKES_DESC -> new OrderSpecifier<?>[] {
                    review.likeCount.desc(),
                    review.id.desc()
            };
            case NEWEST -> new OrderSpecifier<?>[] {
                    review.id.desc()
            };
            case OLDEST -> new OrderSpecifier<?>[] {
                    review.id.asc()
            };
        };
    }
}
