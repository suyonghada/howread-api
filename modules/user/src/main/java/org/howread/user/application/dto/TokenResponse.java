package org.howread.user.application.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}
