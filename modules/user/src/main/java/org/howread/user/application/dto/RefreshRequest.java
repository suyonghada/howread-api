package org.howread.user.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "refreshToken을 입력해 주세요.")
        String refreshToken
) {}
