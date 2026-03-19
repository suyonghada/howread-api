package org.howread.book.application;

import org.howread.book.application.dto.BookResponse;
import org.howread.book.application.dto.BookSearchResult;
import org.howread.book.application.port.BookRepository;
import org.howread.book.domain.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookRegistrarTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookRegistrar bookRegistrar;

    private static BookSearchResult createSearchResult(String isbn) {
        return new BookSearchResult(isbn, "클린 코드", "로버트 마틴", "인사이트",
                LocalDate.of(2013, 12, 24), "thumbnail.url", "설명");
    }

    private static Book createTestBook(Long id, String isbn) {
        Book book = Book.create(isbn, "클린 코드", "로버트 마틴", "인사이트",
                LocalDate.of(2013, 12, 24), "thumbnail.url", "설명");
        try {
            var field = Book.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(book, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return book;
    }

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("ISBN이 DB에 없으면 Book을 생성해서 저장하고 반환한다")
        void register_notExists_savesAndReturns() {
            String isbn = "9781234567890";
            BookSearchResult result = createSearchResult(isbn);
            Book saved = createTestBook(1L, isbn);

            given(bookRepository.findByIsbn(isbn)).willReturn(Optional.empty());
            given(bookRepository.save(any(Book.class))).willReturn(saved);

            BookResponse response = bookRegistrar.register(result);

            assertThat(response.isbn()).isEqualTo(isbn);
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("동시 요청으로 이미 저장된 경우 save 없이 기존 Book을 반환한다 (idempotent)")
        void register_alreadyExists_returnsExistingWithoutSave() {
            String isbn = "9781234567890";
            BookSearchResult result = createSearchResult(isbn);
            Book existing = createTestBook(1L, isbn);

            given(bookRepository.findByIsbn(isbn)).willReturn(Optional.of(existing));

            BookResponse response = bookRegistrar.register(result);

            assertThat(response.isbn()).isEqualTo(isbn);
            verify(bookRepository, never()).save(any());
        }
    }
}
