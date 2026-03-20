package org.howread.review.application.dto;

import java.util.List;

public record ReviewPageResponse(
        List<ReviewResponse> data,
        int page,
        int size,
        long totalCount,
        boolean hasNext
) {
    public static ReviewPageResponse of(List<ReviewResponse> data, int page, int size, long totalCount) {
        boolean hasNext = (long) (page + 1) * size < totalCount;
        return new ReviewPageResponse(data, page, size, totalCount, hasNext);
    }
}
