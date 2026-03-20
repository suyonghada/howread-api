package org.howread.review.domain;

/**
 * 텍스트 리뷰 목록 정렬 기준.
 *
 * 별점(Rating)은 별도 엔티티로 분리되었으므로 rating 기준 정렬은 제공하지 않는다.
 */
public enum ReviewSortType {
    LIKES_DESC,  // 좋아요 많은 순 (기본값)
    NEWEST,      // 최신 순
    OLDEST       // 오래된 순
}
