package org.howread.infra.book;

import lombok.RequiredArgsConstructor;
import org.howread.book.application.dto.BookSearchCondition;
import org.howread.book.application.port.BookRepository;
import org.howread.book.domain.Book;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * [Adapter] BookRepository Port를 JPA로 구현.
 *
 * 단순 CRUD는 BookJpaRepository(Spring Data JPA),
 * 동적 조건 검색은 BookQueryRepository(QueryDSL)에 위임한다.
 */
@Repository
@RequiredArgsConstructor
public class BookRepositoryAdapter implements BookRepository {

    private final BookJpaRepository jpaRepository;
    private final BookQueryRepository queryRepository;

    @Override
    public Optional<Book> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return jpaRepository.findByIsbn(isbn);
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return jpaRepository.existsByIsbn(isbn);
    }

    @Override
    public Book save(Book book) {
        return jpaRepository.save(book);
    }

    @Override
    public void delete(Book book) {
        jpaRepository.delete(book);
    }

    @Override
    public List<Book> findBooksBeforeCursor(Long cursorId, int size) {
        return jpaRepository.findBooksBeforeCursor(cursorId, size);
    }

    @Override
    public List<Book> searchBooks(BookSearchCondition condition, Long cursorId, int size) {
        return queryRepository.searchBooks(condition, cursorId, size);
    }
}
