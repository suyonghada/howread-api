package org.howread.book.application.dto;

import java.time.LocalDate;

/**
 * 외부 도서 API 검색 결과를 담는 도메인 VO.
 *
 * record로 선언하여 불변성을 보장한다.
 * infra 계층의 카카오 응답 JSON을 이 타입으로 변환하여 도메인 계층에 전달한다.
 */
public record BookSearchResult(
        String isbn,
        String title,
        String author,
        String publisher,
        LocalDate publishedDate,
        String thumbnailUrl,
        String description
) {
}
