package org.howread.book.application.port;

import org.howread.book.application.dto.BookSearchCondition;
import org.howread.book.domain.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    Optional<Book> findById(Long id);

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    Book save(Book book);

    /**
     * 커서 기반 페이지네이션.
     *
     * cursorId가 null이면 첫 페이지(가장 최근 등록 순)를 반환한다.
     * cursorId가 있으면 해당 id보다 작은 항목을 최신순(id DESC)으로 size개 반환한다.
     *
     * @param cursorId 마지막으로 받은 bookId, null이면 첫 페이지
     * @param size     한 번에 가져올 항목 수
     */
    List<Book> findBooksBeforeCursor(Long cursorId, int size);

    /**
     * 조건 기반 DB 검색 + 커서 페이지네이션.
     *
     * title, author, isbn 조건을 AND로 결합하며, 각 조건은 부분 일치(LIKE)를 지원한다.
     * isbn은 정확히 일치(=)로 검색한다.
     *
     * @param condition 검색 조건 (null 또는 빈 값인 필드는 무시)
     * @param cursorId  커서 (null이면 첫 페이지)
     * @param size      반환할 최대 항목 수
     */
    List<Book> searchBooks(BookSearchCondition condition, Long cursorId, int size);
}
