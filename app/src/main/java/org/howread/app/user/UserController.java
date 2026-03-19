package org.howread.app.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.howread.common.response.ApiResponse;
import org.howread.user.application.UserService;
import org.howread.user.application.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 프로필 관리 컨트롤러.
 *
 * 모든 /api/v1/users/me/** 엔드포인트는 JWT 인증이 필요하다.
 * SecurityConfig의 .anyRequest().authenticated() 규칙이 이를 보장한다.
 *
 * @AuthenticationPrincipal Long userId: JwtAuthenticationFilter가 SecurityContext에
 * 저장한 인증 주체(userId)를 주입받는다.
 */
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 내 프로필 조회 */
    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile(userId)));
    }

    /** 비밀번호 변경 */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** 닉네임 변경 */
    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<Void>> changeNickname(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangeNicknameRequest request) {
        userService.changeNickname(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** 프로필 이미지 변경 (multipart/form-data) */
    @PutMapping("/profile-image")
    public ResponseEntity<ApiResponse<String>> changeProfileImage(
            @AuthenticationPrincipal Long userId,
            @RequestParam("file") MultipartFile file) {
        String url = userService.changeProfileImage(userId, file);
        return ResponseEntity.ok(ApiResponse.success(url));
    }
}
