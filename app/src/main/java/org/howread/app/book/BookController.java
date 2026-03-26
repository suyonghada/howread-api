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
 * GET 엔드포인트는 비인증 허용, POST는 JWT 인증 필요 (SecurityConfig 참고).
 */
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private static final int MAX_PAGE_SIZE = 100;

    private final BookService bookService;

    /**
     * 외부 API(카카오)로 책 검색.
     * DB에 저장하지 않고 카카오 검색 결과만 반환한다.
     */
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

    /**
     * 책 삭제 (관리자 전용).
     * 리뷰 또는 평점이 존재하면 409 Conflict 반환.
     * SecurityConfig에서 ADMIN 권한만 허용하도록 설정되어 있다.
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /** DB에서 책 단건 조회 */
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookResponse>> getBook(
            @PathVariable Long bookId) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getBook(bookId)));
    }

    /**
     * DB 내 책 목록 조회 (커서 페이지네이션).
     *
     * title, author, isbn 파라미터를 조합하여 DB 내에서 검색할 수 있다.
     * 조건이 없으면 전체 목록을 최신순으로 반환한다.
     * 각 조건은 AND로 결합되며, title/author는 부분 일치, isbn은 정확히 일치한다.
     *
     * @param title  제목 검색어 (부분 일치)
     * @param author 저자 검색어 (부분 일치)
     * @param isbn   ISBN (정확히 일치)
     * @param cursor 마지막으로 받은 bookId (null이면 첫 페이지)
     * @param size   페이지 크기 (기본값 20, 최대 100)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<BookCursorPageResponse>> getBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        BookSearchCondition condition = new BookSearchCondition(title, author, isbn);
        int clampedSize = Math.min(size, MAX_PAGE_SIZE);
        return ResponseEntity.ok(ApiResponse.success(bookService.getBooks(condition, cursor, clampedSize)));
    }
}
