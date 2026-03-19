package org.howread.infra.book;

import lombok.extern.slf4j.Slf4j;
import org.howread.book.application.BookErrorCode;
import org.howread.book.application.dto.BookSearchResult;
import org.howread.book.application.port.BookSearchPort;
import org.howread.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * [Adapter] 카카오 도서 검색 API를 사용하는 BookSearchPort 구현체.
 *
 * Spring 6의 RestClient를 사용한다 (RestTemplate의 후속).
 * API 응답 파싱 후 도메인 VO(BookSearchResult)로 변환하여
 * 도메인 계층이 카카오 API 스펙을 알지 못하도록 격리한다.
 */
@Slf4j
@Component
public class KakaoBookSearchAdapter implements BookSearchPort {

    private static final DateTimeFormatter KAKAO_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestClient restClient;
    private final String bookSearchUrl;

    public KakaoBookSearchAdapter(
            @Value("${kakao.api.key}") String apiKey,
            @Value("${kakao.api.book-search-url}") String bookSearchUrl) {
        this.bookSearchUrl = bookSearchUrl;
        this.restClient = RestClient.builder()
                .defaultHeader("Authorization", "KakaoAK " + apiKey)
                .build();
    }

    @Override
    public List<BookSearchResult> search(String query) {
        try {
            KakaoBookSearchResponse response = restClient.get()
                    .uri(bookSearchUrl + "?query={query}", query)
                    .retrieve()
                    .body(KakaoBookSearchResponse.class);

            if (response == null || response.documents() == null) {
                return List.of();
            }
            return response.documents().stream()
                    .map(this::toBookSearchResult)
                    .toList();
        } catch (RestClientException e) {
            log.error("카카오 도서 API 검색 실패: query={}", query, e);
            throw new BusinessException(BookErrorCode.EXTERNAL_API_ERROR);
        }
    }

    @Override
    public Optional<BookSearchResult> findByIsbn(String isbn) {
        try {
            KakaoBookSearchResponse response = restClient.get()
                    .uri(bookSearchUrl + "?query={isbn}&target=isbn", isbn)
                    .retrieve()
                    .body(KakaoBookSearchResponse.class);

            if (response == null || response.documents() == null || response.documents().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(toBookSearchResult(response.documents().getFirst()));
        } catch (RestClientException e) {
            log.error("카카오 도서 API ISBN 조회 실패: isbn={}", isbn, e);
            throw new BusinessException(BookErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private BookSearchResult toBookSearchResult(KakaoDocument doc) {
        String isbn = extractIsbn(doc.isbn());
        LocalDate publishedDate = parseDate(doc.datetime());
        String author = String.join(", ", doc.authors());

        return new BookSearchResult(
                isbn,
                doc.title(),
                author,
                doc.publisher(),
                publishedDate,
                doc.thumbnail(),
                doc.contents()
        );
    }

    /**
     * 카카오 API의 ISBN은 "ISBN10 ISBN13" 형태로 반환된다.
     * ISBN-13을 우선 사용하고, 없으면 ISBN-10을 사용한다.
     */
    private String extractIsbn(String isbnField) {
        if (isbnField == null || isbnField.isBlank()) return "";
        String[] parts = isbnField.trim().split("\\s+");
        for (String part : parts) {
            if (part.length() == 13) return part;
        }
        return parts[0];
    }

    private LocalDate parseDate(String datetime) {
        if (datetime == null || datetime.isBlank()) return null;
        try {
            // 카카오 API는 "2023-01-01T00:00:00.000+09:00" 형태로 반환
            return LocalDate.parse(datetime.substring(0, 10), KAKAO_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // 카카오 API 응답 매핑용 내부 record
    record KakaoBookSearchResponse(List<KakaoDocument> documents) {}

    record KakaoDocument(
            String title,
            String contents,
            String isbn,
            String datetime,
            List<String> authors,
            String publisher,
            String thumbnail
    ) {}
}
