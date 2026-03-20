package org.howread.book.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class BookTest {

    private static Book createBook() {
        return Book.create("9788966261208", "클린 코드", "로버트 C. 마틴",
                "인사이트", LocalDate.of(2023, 1, 1), null, null);
    }

    @Nested
    @DisplayName("addRating()")
    class AddRating {

        @Test
        @DisplayName("첫 번째 별점이면 averageRating이 해당 평점과 같다")
        void addRating_firstRating_averageEqualsRating() {
            Book book = createBook();

            book.addRating(4);

            assertThat(book.getRatingCount()).isEqualTo(1);
            assertThat(book.getAverageRating()).isEqualTo(4.0);
        }

        @Test
        @DisplayName("여러 별점을 추가하면 평균이 올바르게 계산된다")
        void addRating_multipleRatings_correctAverage() {
            Book book = createBook();

            book.addRating(5);
            book.addRating(3);
            book.addRating(4);

            assertThat(book.getRatingCount()).isEqualTo(3);
            assertThat(book.getAverageRating()).isCloseTo(4.0, within(0.001));
        }
    }

    @Nested
    @DisplayName("removeRating()")
    class RemoveRating {

        @Test
        @DisplayName("마지막 별점을 삭제하면 averageRating=0.0, ratingCount=0이 된다")
        void removeRating_lastRating_resetsToZero() {
            Book book = createBook();
            book.addRating(4);

            book.removeRating(4);

            assertThat(book.getRatingCount()).isEqualTo(0);
            assertThat(book.getAverageRating()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("여러 별점 중 하나를 삭제하면 평균이 올바르게 재계산된다")
        void removeRating_oneOfMany_correctAverage() {
            Book book = createBook();
            book.addRating(5);
            book.addRating(3);  // 평균 4.0

            book.removeRating(3);  // 5만 남음

            assertThat(book.getRatingCount()).isEqualTo(1);
            assertThat(book.getAverageRating()).isCloseTo(5.0, within(0.001));
        }
    }

    @Nested
    @DisplayName("changeRating()")
    class ChangeRating {

        @Test
        @DisplayName("평점 변경 시 평균이 올바르게 재계산된다")
        void changeRating_correctRecalculation() {
            Book book = createBook();
            book.addRating(5);
            book.addRating(3);  // 평균 4.0

            book.changeRating(3, 5);  // 3을 5로 변경 → 5, 5

            assertThat(book.getRatingCount()).isEqualTo(2);
            assertThat(book.getAverageRating()).isCloseTo(5.0, within(0.001));
        }

        @Test
        @DisplayName("별점이 없으면 아무것도 변경하지 않는다")
        void changeRating_noRatings_noChange() {
            Book book = createBook();

            book.changeRating(3, 5);

            assertThat(book.getRatingCount()).isEqualTo(0);
            assertThat(book.getAverageRating()).isEqualTo(0.0);
        }
    }

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
