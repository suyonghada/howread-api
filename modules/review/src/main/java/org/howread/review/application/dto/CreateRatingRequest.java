package org.howread.review.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CreateRatingRequest(
        @Min(value = 1, message = "별점은 최소 1점입니다.")
        @Max(value = 5, message = "별점은 최대 5점입니다.")
        int rating
) {
}
