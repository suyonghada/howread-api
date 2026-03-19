package org.howread.book.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BookTest {

    @Nested
    @DisplayName("Book.create()")
    class Create {

        @Test
        @DisplayName("모든 필드가 정상적으로 설정된다")
        void create_allFieldsSet() {
            LocalDate publishedDate = LocalDate.of(2023, 1, 1);

            Book book = Book.create(
                    "9788966261208", "클린 코드", "로버트 C. 마틴",
                    "인사이트", publishedDate,
                    "https://thumbnail.url", "좋은 코드를 작성하는 방법"
            );

            assertThat(book.getIsbn()).isEqualTo("9788966261208");
            assertThat(book.getTitle()).isEqualTo("클린 코드");
            assertThat(book.getAuthor()).isEqualTo("로버트 C. 마틴");
            assertThat(book.getPublisher()).isEqualTo("인사이트");
            assertThat(book.getPublishedDate()).isEqualTo(publishedDate);
            assertThat(book.getThumbnailUrl()).isEqualTo("https://thumbnail.url");
            assertThat(book.getDescription()).isEqualTo("좋은 코드를 작성하는 방법");
        }

        @Test
        @DisplayName("선택 필드(publisher, publishedDate, thumbnailUrl, description)는 null을 허용한다")
        void create_optionalFieldsCanBeNull() {
            Book book = Book.create("9788966261208", "클린 코드", "로버트 C. 마틴",
                    null, null, null, null);

            assertThat(book.getIsbn()).isEqualTo("9788966261208");
            assertThat(book.getTitle()).isEqualTo("클린 코드");
            assertThat(book.getPublisher()).isNull();
            assertThat(book.getPublishedDate()).isNull();
            assertThat(book.getThumbnailUrl()).isNull();
            assertThat(book.getDescription()).isNull();
        }
    }
}
