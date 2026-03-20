package org.howread.review.application.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateReviewRequest(
        @NotBlank(message = "리뷰 내용을 입력해주세요.")
        String content
) {
}
