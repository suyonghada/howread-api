package org.howread.book.application.port;

import org.howread.book.domain.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    Optional<Book> findById(Long id);

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByTitleContaining(String keyword);

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
}
