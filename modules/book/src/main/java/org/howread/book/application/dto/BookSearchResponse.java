package org.howread.book.application.dto;

import java.time.LocalDate;

/** 외부 API 검색 결과를 클라이언트에 반환할 때 사용하는 응답 DTO. */
public record BookSearchResponse(
        String isbn,
        String title,
        String author,
        String publisher,
        LocalDate publishedDate,
        String thumbnailUrl,
        String description
) {
    public static BookSearchResponse from(BookSearchResult result) {
        return new BookSearchResponse(
                result.isbn(),
                result.title(),
                result.author(),
                result.publisher(),
                result.publishedDate(),
                result.thumbnailUrl(),
                result.description()
        );
    }
}
