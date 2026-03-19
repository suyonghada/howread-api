package org.howread.book.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.howread.book.application.dto.BookCursorPageResponse;
import org.howread.book.application.dto.BookResponse;
import org.howread.book.application.dto.BookSearchCondition;
import org.howread.book.application.dto.BookSearchResponse;
import org.howread.book.application.dto.BookSearchResult;
import org.howread.book.application.port.BookRepository;
import org.howread.book.application.port.BookSearchPort;
import org.howread.book.domain.Book;
import org.howread.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Book 도메인의 핵심 Use Case를 조율하는 Application Service.
 * <p>
 * 외부 도서 API 검색과 내부 DB 관리를 분리하여 책임을 명확히 한다. - searchBooks: 외부 API 결과만 반환 (DB 저장 없음) - registerBook: ISBN으로 외부 API 조회 후
 * DB에 등록 (idempotent)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final BookSearchPort bookSearchPort;
    private final BookRegistrar bookRegistrar;

    /**
     * 외부 API로 책 검색. DB에 저장하지 않고 검색 결과만 반환한다.
     */
    public List<BookSearchResponse> searchBooks(String query) {
        return bookSearchPort.search(query)
                .stream()
                .map(BookSearchResponse::from)
                .toList();
    }

    /**
     * ISBN으로 책을 DB에 등록한다 (idempotent).
     * <p>
     * DB 조회 → 이미 있으면 즉시 반환, 없으면 외부 API 조회 후 저장한다.
     * <p>
     * 외부 API 호출을 @Transactional 범위 밖에서 수행한다. 카카오 API 응답이 1~2초 걸리는 동안 DB 커넥션을 점유하면 동시 요청이 많을 때 커넥션 풀이 고갈되기 때문이다.
     */
    public BookResponse registerBook(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .map(BookResponse::from)
                .orElseGet(() -> {
                    // 트랜잭션 밖에서 외부 API 호출
                    BookSearchResult result = bookSearchPort.findByIsbn(isbn)
                            .orElseThrow(() -> new BusinessException(BookErrorCode.BOOK_NOT_FOUND));
                    return bookRegistrar.register(result);
                });
    }

    /**
     * DB에서 책 단건 조회.
     */
    public BookResponse getBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(BookErrorCode.BOOK_NOT_FOUND));
        return BookResponse.from(book);
    }

    /**
     * 책 목록 조회 (커서 페이지네이션).
     * <p>
     * 검색 조건이 하나라도 있으면 QueryDSL 동적 쿼리로 필터링하고, 조건이 없으면 단순 커서 조회를 사용한다. 두 경로 모두 동일한 커서 페이지네이션 응답을 반환한다.
     */
    public BookCursorPageResponse getBooks(BookSearchCondition condition, Long cursorId, int size) {
        // size+1개를 조회하여 다음 페이지 존재 여부를 판단한다.
        // 실제로 size+1개가 반환되면 hasNext=true, 그렇지 않으면 마지막 페이지다.
        int fetchSize = size + 1;
        List<Book> books = condition.isEmpty()
                ? bookRepository.findBooksBeforeCursor(cursorId, fetchSize)
                : bookRepository.searchBooks(condition, cursorId, fetchSize);

        List<BookResponse> responses = books.stream().map(BookResponse::from).toList();
        return BookCursorPageResponse.of(responses, size);
    }
}
