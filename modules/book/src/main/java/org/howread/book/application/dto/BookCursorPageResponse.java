package org.howread.book.application.dto;

import java.util.List;

/**
 * 커서 기반 페이지네이션 응답 래퍼.
 *
 * Offset 방식과 달리 커서 방식은 중간에 데이터가 삽입/삭제되어도
 * 누락이나 중복 없이 안정적으로 다음 페이지를 가져올 수 있다.
 *
 * @param data       현재 페이지 데이터
 * @param nextCursor 다음 페이지 커서 (null이면 마지막 페이지)
 * @param hasNext    다음 페이지 존재 여부
 */
public record BookCursorPageResponse(
        List<BookResponse> data,
        Long nextCursor,
        boolean hasNext
) {
    public static BookCursorPageResponse of(List<BookResponse> data, int requestedSize) {
        boolean hasNext = data.size() == requestedSize;
        Long nextCursor = hasNext ? data.getLast().id() : null;
        return new BookCursorPageResponse(data, nextCursor, hasNext);
    }
}
