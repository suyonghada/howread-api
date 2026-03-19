package org.howread.app.book;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.howread.book.application.BookService;
import org.howread.book.application.dto.*;
import org.howread.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 도서 관련 API 엔드포인트.
 * 모든 엔드포인트는 JWT 인증이 필요하다 (SecurityConfig에서 설정).
 */
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final BookService bookService;

    /** 외부 API(카카오)로 책 검색. DB에 저장하지 않고 결과만 반환한다. */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BookSearchResponse>>> searchBooks(
            @RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.success(bookService.searchBooks(query)));
    }

    /**
     * ISBN으로 책을 DB에 등록한다.
     * 이미 등록된 경우 기존 데이터를 반환하므로 클라이언트는 중복 여부를 신경 쓰지 않아도 된다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> registerBook(
            @RequestBody @Valid RegisterBookRequest request) {
        BookResponse response = bookService.registerBook(request.isbn());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /** DB에서 책 단건 조회 */
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookResponse>> getBook(
            @PathVariable Long bookId) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getBook(bookId)));
    }

    /**
     * 커서 기반 페이지네이션으로 책 목록 조회.
     *
     * @param cursor 마지막으로 받은 bookId (null이면 첫 페이지)
     * @param size   페이지 크기 (기본값 20, 최대 100)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<BookCursorPageResponse>> getBooks(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        int clampedSize = Math.min(size, MAX_PAGE_SIZE);
        return ResponseEntity.ok(ApiResponse.success(bookService.getBooks(cursor, clampedSize)));
    }
}
