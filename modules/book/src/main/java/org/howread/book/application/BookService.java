package org.howread.book.application;

import lombok.RequiredArgsConstructor;
import org.howread.book.application.dto.*;
import org.howread.book.application.port.BookRepository;
import org.howread.book.application.port.BookSearchPort;
import org.howread.book.domain.Book;
import org.howread.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Book 도메인의 핵심 Use Case를 조율하는 Application Service.
 *
 * 외부 도서 API 검색과 내부 DB 관리를 분리하여 책임을 명확히 한다.
 * - searchBooks: 외부 API 결과만 반환 (DB 저장 없음)
 * - registerBook: ISBN으로 외부 API 조회 후 DB에 등록 (idempotent)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final BookSearchPort bookSearchPort;

    /** 외부 API로 책 검색. DB에 저장하지 않고 검색 결과만 반환한다. */
    public List<BookSearchResponse> searchBooks(String query) {
        return bookSearchPort.search(query)
                .stream()
                .map(BookSearchResponse::from)
                .toList();
    }

    /**
     * ISBN으로 책을 DB에 등록한다 (idempotent).
     *
     * 이미 등록된 ISBN이면 기존 Book을 반환하고,
     * 없으면 외부 API에서 조회 후 DB에 저장한다.
     * 이렇게 하면 클라이언트가 중복 등록을 신경 쓰지 않아도 되고,
     * 검색 결과에서 바로 "등록" 버튼을 눌러도 안전하다.
     */
    @Transactional
    public BookResponse registerBook(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .map(BookResponse::from)
                .orElseGet(() -> {
                    BookSearchResult result = bookSearchPort.findByIsbn(isbn)
                            .orElseThrow(() -> new BusinessException(BookErrorCode.BOOK_NOT_FOUND));
                    Book book = Book.create(
                            result.isbn(), result.title(), result.author(),
                            result.publisher(), result.publishedDate(),
                            result.thumbnailUrl(), result.description()
                    );
                    return BookResponse.from(bookRepository.save(book));
                });
    }

    /** DB에서 책 단건 조회. */
    public BookResponse getBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(BookErrorCode.BOOK_NOT_FOUND));
        return BookResponse.from(book);
    }

    /**
     * 책 목록 조회 (커서 페이지네이션).
     *
     * 검색 조건이 하나라도 있으면 QueryDSL 동적 쿼리로 필터링하고,
     * 조건이 없으면 단순 커서 조회를 사용한다.
     * 두 경로 모두 동일한 커서 페이지네이션 응답을 반환한다.
     */
    public BookCursorPageResponse getBooks(BookSearchCondition condition, Long cursorId, int size) {
        List<Book> books = condition.isEmpty()
                ? bookRepository.findBooksBeforeCursor(cursorId, size)
                : bookRepository.searchBooks(condition, cursorId, size);

        List<BookResponse> responses = books.stream().map(BookResponse::from).toList();
        return BookCursorPageResponse.of(responses, size);
    }
}
