package org.howread.infra.book;

import lombok.RequiredArgsConstructor;
import org.howread.book.application.port.BookRepository;
import org.howread.book.domain.Book;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * [Adapter] BookRepository Port를 JPA로 구현.
 *
 * 기술 교체 시 이 클래스만 변경한다.
 */
@Repository
@RequiredArgsConstructor
public class BookRepositoryAdapter implements BookRepository {

    private final BookJpaRepository jpaRepository;

    @Override
    public Optional<Book> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return jpaRepository.findByIsbn(isbn);
    }

    @Override
    public List<Book> findByTitleContaining(String keyword) {
        return jpaRepository.findByTitleContaining(keyword);
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
    public List<Book> findBooksBeforeCursor(Long cursorId, int size) {
        return jpaRepository.findBooksBeforeCursor(cursorId, size);
    }
}
