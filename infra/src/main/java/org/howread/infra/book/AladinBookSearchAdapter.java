package org.howread.infra.book;

import lombok.extern.slf4j.Slf4j;
import org.howread.book.application.BookErrorCode;
import org.howread.book.application.dto.BookSearchResult;
import org.howread.book.application.port.BookSearchPort;
import org.howread.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * [Adapter] 알라딘 Open API(TTB API)를 사용하는 BookSearchPort 구현체.
 *
 * 카카오 API와 달리 인증은 Authorization 헤더가 아니라
 * ttbkey 쿼리 파라미터로 전달하며, 검색과 ISBN 조회 엔드포인트가 분리되어 있다.
 * API 응답 파싱 후 도메인 VO(BookSearchResult)로 변환하여
 * 도메인 계층이 알라딘 API 스펙을 알지 못하도록 격리한다.
 */
@Slf4j
@Component
public class AladinBookSearchAdapter implements BookSearchPort {

    /**
     * 알라딘 저자 필드는 "홍길동 (지은이), 김철수 (옮긴이)" 형태로 반환된다.
     * 괄호와 그 안의 역할 텍스트를 제거하기 위한 패턴.
     */
    private static final Pattern AUTHOR_ROLE_PATTERN = Pattern.compile("\\s*\\([^)]*\\)");

    private final RestClient restClient;
    private final String ttbkey;
    private final String searchUrl;
    private final String lookupUrl;

    public AladinBookSearchAdapter(
            @Value("${aladin.api.ttbkey}") String ttbkey,
            @Value("${aladin.api.search-url}") String searchUrl,
            @Value("${aladin.api.lookup-url}") String lookupUrl) {
        this.ttbkey = ttbkey;
        this.searchUrl = searchUrl;
        this.lookupUrl = lookupUrl;
        this.restClient = RestClient.builder()
                .requestFactory(createRequestFactory())
                .build();
    }

    @Override
    public List<BookSearchResult> search(String query) {
        try {
            AladinBookSearchResponse response = restClient.get()
                    .uri(searchUrl + "?ttbkey={key}&Query={query}" +
                            "&QueryType=Keyword&SearchTarget=Book" +
                            "&Output=js&Version=20131101&Cover=Big&MaxResults=20",
                            ttbkey, query)
                    .retrieve()
                    .body(AladinBookSearchResponse.class);

            if (response == null || response.item() == null) {
                return List.of();
            }
            return response.item().stream()
                    .map(this::toBookSearchResult)
                    .toList();
        } catch (RestClientException e) {
            log.error("알라딘 도서 API 검색 실패: query={}", query, e);
            throw new BusinessException(BookErrorCode.EXTERNAL_API_ERROR);
        }
    }

    @Override
    public Optional<BookSearchResult> findByIsbn(String isbn) {
        try {
            AladinBookSearchResponse response = restClient.get()
                    .uri(lookupUrl + "?ttbkey={key}&ItemId={isbn}" +
                            "&IdType=ISBN13&Output=js&Version=20131101&Cover=Big",
                            ttbkey, isbn)
                    .retrieve()
                    .body(AladinBookSearchResponse.class);

            if (response == null || response.item() == null || response.item().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(toBookSearchResult(response.item().getFirst()));
        } catch (RestClientException e) {
            log.error("알라딘 도서 API ISBN 조회 실패: isbn={}", isbn, e);
            throw new BusinessException(BookErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private BookSearchResult toBookSearchResult(AladinItem item) {
        String isbn = extractIsbn(item.isbn13(), item.isbn());
        LocalDate publishedDate = parseDate(item.pubDate());
        String author = cleanAuthor(item.author());

        return new BookSearchResult(
                isbn,
                item.title(),
                author,
                item.publisher(),
                publishedDate,
                item.cover(),
                item.description()
        );
    }

    /**
     * 연결 타임아웃 3초, 읽기 타임아웃 5초.
     * 외부 API 지연이 무한정 커넥션을 점유하지 못하도록 제한한다.
     */
    private static SimpleClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return factory;
    }

    /**
     * 알라딘 API는 isbn13(13자리)과 isbn(10자리)을 별도 필드로 제공한다.
     * ISBN-13을 우선 사용하고, 없으면 ISBN-10을 사용한다.
     */
    private String extractIsbn(String isbn13, String isbn10) {
        if (isbn13 != null && !isbn13.isBlank()) return isbn13.trim();
        if (isbn10 != null && !isbn10.isBlank()) return isbn10.trim();
        return "";
    }

    private LocalDate parseDate(String pubDate) {
        if (pubDate == null || pubDate.isBlank()) return null;
        try {
            // 알라딘 API는 "2023-01-01" 형태로 반환
            return LocalDate.parse(pubDate.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 알라딘 author 필드는 "홍길동 (지은이), 김철수 (옮긴이)" 형태로 반환된다.
     * 괄호 안의 역할 텍스트를 제거하고 이름만 남긴다.
     * 예: "홍길동 (지은이), 김철수 (옮긴이)" → "홍길동, 김철수"
     */
    private String cleanAuthor(String author) {
        if (author == null || author.isBlank()) return "";
        Matcher matcher = AUTHOR_ROLE_PATTERN.matcher(author);
        return matcher.replaceAll("").trim();
    }

    // 알라딘 API 응답 매핑용 내부 record
    record AladinBookSearchResponse(List<AladinItem> item) {}

    record AladinItem(
            String title,
            String author,
            String publisher,
            String pubDate,
            String isbn,
            String isbn13,
            String cover,
            String description
    ) {}
}
