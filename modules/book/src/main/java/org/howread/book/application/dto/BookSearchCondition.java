package org.howread.book.application.dto;

/**
 * DB 내 책 검색 조건.
 *
 * 모든 필드는 선택적이며, null 또는 빈 문자열이면 해당 조건을 무시한다.
 * 여러 조건이 동시에 있으면 AND로 결합된다.
 */
public record BookSearchCondition(
        String title,
        String author,
        String isbn
) {
    public boolean isEmpty() {
        return isBlank(title) && isBlank(author) && isBlank(isbn);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
