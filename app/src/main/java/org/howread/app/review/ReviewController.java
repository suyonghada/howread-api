package org.howread.app.review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.howread.common.response.ApiResponse;
import org.howread.review.application.ReviewService;
import org.howread.review.application.dto.CreateReviewRequest;
import org.howread.review.application.dto.ReviewPageResponse;
import org.howread.review.application.dto.ReviewResponse;
import org.howread.review.application.dto.UpdateReviewRequest;
import org.howread.review.domain.ReviewSortType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ReviewService reviewService;

    @PostMapping("/api/v1/books/{bookId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long bookId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.createReview(userId, bookId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/api/v1/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateReviewRequest request) {
        ReviewResponse response = reviewService.updateReview(userId, reviewId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/api/v1/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Long userId) {
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 책의 텍스트 리뷰 목록 조회.
     * 비인증 사용자도 접근 가능하며, 비인증 시 리뷰 본문이 마스킹(isBlurred=true)된다.
     */
    @GetMapping("/api/v1/books/{bookId}/reviews")
    public ResponseEntity<ApiResponse<ReviewPageResponse>> getBookReviews(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "LIKES_DESC") ReviewSortType sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Long currentUserId) {
        int clampedSize = Math.min(size, MAX_PAGE_SIZE);
        ReviewPageResponse response = reviewService.getBookReviews(bookId, sort, page, clampedSize, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/users/me/reviews")
    public ResponseEntity<ApiResponse<ReviewPageResponse>> getMyReviews(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int clampedSize = Math.min(size, MAX_PAGE_SIZE);
        ReviewPageResponse response = reviewService.getMyReviews(userId, page, clampedSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/v1/reviews/{reviewId}/likes")
    public ResponseEntity<ApiResponse<Void>> likeReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Long userId) {
        reviewService.likeReview(userId, reviewId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/api/v1/reviews/{reviewId}/likes")
    public ResponseEntity<ApiResponse<Void>> unlikeReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Long userId) {
        reviewService.unlikeReview(userId, reviewId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
