package org.howread.user.application.dto;

import java.time.LocalDateTime;

public record UserProfileResponse(
        String email,
        String nickname,
        String profileImageUrl,
        String role,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {}
