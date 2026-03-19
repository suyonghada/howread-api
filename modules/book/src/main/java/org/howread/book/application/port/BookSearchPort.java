package org.howread.book.application.port;

import org.howread.book.application.dto.BookSearchResult;

import java.util.List;
import java.util.Optional;

/**
 * 외부 도서 검색 API 추상화 Port.
 *
 * 구현체(KakaoBookSearchAdapter)는 infra에 위치하며,
 * 이 인터페이스 덕분에 도메인 모듈은 카카오 API를 전혀 알지 못한다.
 * 추후 네이버 도서 API 등으로 교체 시 구현체만 바꾸면 된다.
 */
public interface BookSearchPort {

    /** 제목/저자 키워드로 외부 API 검색 */
    List<BookSearchResult> search(String query);

    /** ISBN으로 외부 API에서 단건 조회 */
    Optional<BookSearchResult> findByIsbn(String isbn);
}
