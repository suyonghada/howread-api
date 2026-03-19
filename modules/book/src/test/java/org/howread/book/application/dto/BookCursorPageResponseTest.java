package org.howread.book.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class BookCursorPageResponseTest {

    private static BookResponse fakeResponse(long id) {
        return new BookResponse(id, "isbn-" + id, "제목" + id, "저자", null, null, null, null);
    }

    @Nested
    @DisplayName("of() — hasNext=true (다음 페이지 있음)")
    class HasNextTrue {

        @Test
        @DisplayName("rawData가 requestedSize+1이면 hasNext=true, data는 requestedSize개, nextCursor는 마지막 id")
        void of_rawDataExceedsRequestedSize() {
            // size=3 요청 → 4개 조회됨 (size+1 패턴)
            List<BookResponse> rawData = List.of(
                    fakeResponse(10), fakeResponse(9), fakeResponse(8), fakeResponse(7)
            );

            BookCursorPageResponse response = BookCursorPageResponse.of(rawData, 3);

            assertThat(response.hasNext()).isTrue();
            assertThat(response.data()).hasSize(3);
            assertThat(response.data()).extracting(BookResponse::id).containsExactly(10L, 9L, 8L);
            assertThat(response.nextCursor()).isEqualTo(8L); // 남은 data의 마지막 id
        }
    }

    @Nested
    @DisplayName("of() — hasNext=false (마지막 페이지)")
    class HasNextFalse {

        @Test
        @DisplayName("rawData가 requestedSize와 같으면 hasNext=false, nextCursor=null")
        void of_rawDataEqualsRequestedSize() {
            List<BookResponse> rawData = List.of(
                    fakeResponse(10), fakeResponse(9), fakeResponse(8)
            );

            BookCursorPageResponse response = BookCursorPageResponse.of(rawData, 3);

            assertThat(response.hasNext()).isFalse();
            assertThat(response.data()).hasSize(3);
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("rawData가 requestedSize보다 작으면 hasNext=false, nextCursor=null")
        void of_rawDataLessThanRequestedSize() {
            List<BookResponse> rawData = List.of(fakeResponse(10), fakeResponse(9));

            BookCursorPageResponse response = BookCursorPageResponse.of(rawData, 3);

            assertThat(response.hasNext()).isFalse();
            assertThat(response.data()).hasSize(2);
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("rawData가 비어 있으면 hasNext=false, data=[], nextCursor=null")
        void of_emptyRawData() {
            BookCursorPageResponse response = BookCursorPageResponse.of(Collections.emptyList(), 3);

            assertThat(response.hasNext()).isFalse();
            assertThat(response.data()).isEmpty();
            assertThat(response.nextCursor()).isNull();
        }
    }
}
