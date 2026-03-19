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
    /**
     * size+1개를 조회한 결과로 응답을 만든다.
     *
     * rawData가 size+1개이면 다음 페이지가 있다는 뜻이므로 마지막 요소를 제거하고
     * 남은 마지막 요소의 id를 nextCursor로 설정한다.
     * size+1개 미만이면 마지막 페이지이므로 nextCursor는 null이다.
     *
     * @param rawData       size+1개를 요청하여 받은 원본 데이터
     * @param requestedSize 클라이언트가 요청한 실제 페이지 크기
     */
    public static BookCursorPageResponse of(List<BookResponse> rawData, int requestedSize) {
        boolean hasNext = rawData.size() > requestedSize;
        List<BookResponse> data = hasNext ? rawData.subList(0, requestedSize) : rawData;
        Long nextCursor = hasNext ? data.getLast().id() : null;
        return new BookCursorPageResponse(data, nextCursor, hasNext);
    }
}
