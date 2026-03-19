package org.howread.book.application.dto;

import jakarta.validation.constraints.NotBlank;

/** ISBN으로 DB에 책 등록 요청 DTO. */
public record RegisterBookRequest(
        @NotBlank(message = "ISBN은 필수입니다.") String isbn
) {
}
