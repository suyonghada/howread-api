package org.howread.app.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.howread.common.response.ApiResponse;
import org.howread.user.application.UserService;
import org.howread.user.application.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 엔드포인트.
 * /api/v1/auth/** 는 SecurityConfig에서 permitAll 처리되므로 인증 없이 접근 가능하다.
 * logout, withdraw는 JWT가 필요하므로 @AuthenticationPrincipal로 userId를 추출한다.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /** 이메일 중복 확인 */
    @PostMapping("/email/check")
    public ResponseEntity<ApiResponse<Void>> checkEmail(
            @RequestBody @Valid EmailCheckRequest request) {
        userService.checkEmailDuplicate(request.email());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /** 이메일 인증번호 발송 */
    @PostMapping("/email/send-code")
    public ResponseEntity<ApiResponse<Void>> sendCode(
            @RequestBody @Valid EmailCheckRequest request) {
        userService.sendVerificationCode(request.email());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /** 인증번호 검증 */
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyCode(
            @RequestBody @Valid EmailVerifyRequest request) {
        userService.verifyEmailCode(request.email(), request.code());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /** 회원가입 */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TokenResponse>> register(
            @RequestBody @Valid RegisterRequest request) {
        TokenResponse tokens = userService.register(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(tokens));
    }

    /** 로그인 */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @RequestBody @Valid LoginRequest request) {
        TokenResponse tokens = userService.login(request.email(), request.password());
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }

    /** AccessToken 재발급 */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> refresh(
            @RequestBody @Valid RefreshRequest request) {
        AccessTokenResponse response = userService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** 로그아웃 — RefreshToken 삭제 */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal Long userId) {
        userService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /** 회원탈퇴 (Soft Delete) */
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal Long userId) {
        userService.withdraw(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
