package org.howread.book.application;

import org.howread.book.application.dto.*;
import org.howread.book.application.port.BookRepository;
import org.howread.book.application.port.BookSearchPort;
import org.howread.book.domain.Book;
import org.howread.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookSearchPort bookSearchPort;

    @InjectMocks
    private BookService bookService;

    private static Book createTestBook(Long id, String isbn) {
        Book book = Book.create(isbn, "클린 코드", "로버트 마틴", "인사이트",
                LocalDate.of(2013, 12, 24), "thumbnail.url", "설명");
        // JPA가 관리하는 id를 리플렉션으로 설정
        try {
            var field = Book.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(book, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return book;
    }

    private static BookSearchResult createSearchResult(String isbn) {
        return new BookSearchResult(isbn, "클린 코드", "로버트 마틴", "인사이트",
                LocalDate.of(2013, 12, 24), "thumbnail.url", "설명");
    }

    @Nested
    @DisplayName("searchBooks()")
    class SearchBooks {

        @Test
        @DisplayName("외부 API 검색 결과를 BookSearchResponse 목록으로 반환한다")
        void searchBooks_returnsExternalApiResults() {
            String query = "클린 코드";
            given(bookSearchPort.search(query)).willReturn(List.of(createSearchResult("9781234567890")));

            List<BookSearchResponse> result = bookService.searchBooks(query);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isbn()).isEqualTo("9781234567890");
            verify(bookSearchPort).search(query);
        }

        @Test
        @DisplayName("검색 결과가 없으면 빈 목록을 반환한다")
        void searchBooks_emptyResult() {
            given(bookSearchPort.search(anyString())).willReturn(List.of());

            List<BookSearchResponse> result = bookService.searchBooks("없는책");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("registerBook()")
    class RegisterBook {

        @Test
        @DisplayName("DB에 이미 존재하는 ISBN이면 외부 API를 호출하지 않고 기존 Book을 반환한다")
        void registerBook_alreadyExists_returnsExisting() {
            String isbn = "9781234567890";
            Book existing = createTestBook(1L, isbn);
            given(bookRepository.findByIsbn(isbn)).willReturn(Optional.of(existing));

            BookResponse result = bookService.registerBook(isbn);

            assertThat(result.isbn()).isEqualTo(isbn);
            verify(bookSearchPort, org.mockito.Mockito.never()).findByIsbn(any());
        }

        @Test
        @DisplayName("DB에 없으면 외부 API 조회 후 저장하고 반환한다")
        void registerBook_notExists_savesAndReturns() {
            String isbn = "9781234567890";
            BookSearchResult searchResult = createSearchResult(isbn);
            Book saved = createTestBook(1L, isbn);

            given(bookRepository.findByIsbn(isbn)).willReturn(Optional.empty());
            given(bookSearchPort.findByIsbn(isbn)).willReturn(Optional.of(searchResult));
            given(bookRepository.save(any(Book.class))).willReturn(saved);

            BookResponse result = bookService.registerBook(isbn);

            assertThat(result.isbn()).isEqualTo(isbn);
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("DB에도 없고 외부 API에도 없으면 BOOK_NOT_FOUND 예외를 던진다")
        void registerBook_notFoundAnywhere_throwsException() {
            String isbn = "9780000000000";
            given(bookRepository.findByIsbn(isbn)).willReturn(Optional.empty());
            given(bookSearchPort.findByIsbn(isbn)).willReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.registerBook(isbn))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("책을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("getBook()")
    class GetBook {

        @Test
        @DisplayName("존재하는 bookId이면 BookResponse를 반환한다")
        void getBook_found_returnsResponse() {
            Book book = createTestBook(1L, "9781234567890");
            given(bookRepository.findById(1L)).willReturn(Optional.of(book));

            BookResponse result = bookService.getBook(1L);

            assertThat(result.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하지 않는 bookId이면 BOOK_NOT_FOUND 예외를 던진다")
        void getBook_notFound_throwsException() {
            given(bookRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.getBook(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("책을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("getBooks()")
    class GetBooks {

        @Test
        @DisplayName("검색 조건이 없으면 findBooksBeforeCursor를 size+1로 호출한다")
        void getBooks_emptyCondition_callsFindBooksBeforeCursor() {
            BookSearchCondition emptyCondition = new BookSearchCondition(null, null, null);
            given(bookRepository.findBooksBeforeCursor(null, 4)).willReturn(List.of());

            bookService.getBooks(emptyCondition, null, 3);

            verify(bookRepository).findBooksBeforeCursor(null, 4); // size+1 = 4
        }

        @Test
        @DisplayName("검색 조건이 있으면 searchBooks를 size+1로 호출한다")
        void getBooks_withCondition_callsSearchBooks() {
            BookSearchCondition condition = new BookSearchCondition("클린", null, null);
            given(bookRepository.searchBooks(eq(condition), isNull(), eq(4))).willReturn(List.of());

            bookService.getBooks(condition, null, 3);

            verify(bookRepository).searchBooks(eq(condition), isNull(), eq(4)); // size+1 = 4
        }

        @Test
        @DisplayName("결과가 size+1개이면 hasNext=true, data는 size개다")
        void getBooks_hasNextTrue_whenFetchSizeExceeded() {
            BookSearchCondition emptyCondition = new BookSearchCondition(null, null, null);
            List<Book> books = List.of(
                    createTestBook(10L, "isbn-10"),
                    createTestBook(9L, "isbn-9"),
                    createTestBook(8L, "isbn-8"),
                    createTestBook(7L, "isbn-7")  // 초과분
            );
            given(bookRepository.findBooksBeforeCursor(null, 4)).willReturn(books);

            BookCursorPageResponse response = bookService.getBooks(emptyCondition, null, 3);

            assertThat(response.hasNext()).isTrue();
            assertThat(response.data()).hasSize(3);
            assertThat(response.nextCursor()).isEqualTo(8L);
        }

        @Test
        @DisplayName("결과가 size개 이하이면 hasNext=false, nextCursor=null이다")
        void getBooks_hasNextFalse_whenLastPage() {
            BookSearchCondition emptyCondition = new BookSearchCondition(null, null, null);
            List<Book> books = List.of(createTestBook(5L, "isbn-5"), createTestBook(4L, "isbn-4"));
            given(bookRepository.findBooksBeforeCursor(null, 4)).willReturn(books);

            BookCursorPageResponse response = bookService.getBooks(emptyCondition, null, 3);

            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }
    }
}
