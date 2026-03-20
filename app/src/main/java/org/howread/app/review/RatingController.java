package org.howread.app.review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.howread.common.response.ApiResponse;
import org.howread.review.application.RatingService;
import org.howread.review.application.dto.CreateRatingRequest;
import org.howread.review.application.dto.RatingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books/{bookId}/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    /**
     * 별점을 등록하거나 수정한다 (upsert).
     * 이미 별점이 있으면 수정, 없으면 생성한다.
     */
    @PutMapping
    public ResponseEntity<ApiResponse<RatingResponse>> upsertRating(
            @PathVariable Long bookId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateRatingRequest request) {
        RatingResponse response = ratingService.upsertRating(userId, bookId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 별점을 삭제한다.
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteRating(
            @PathVariable Long bookId,
            @AuthenticationPrincipal Long userId) {
        ratingService.deleteRating(userId, bookId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 내 별점을 조회한다.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<RatingResponse>> getMyRating(
            @PathVariable Long bookId,
            @AuthenticationPrincipal Long userId) {
        RatingResponse response = ratingService.getMyRating(userId, bookId)
                .orElse(null);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
