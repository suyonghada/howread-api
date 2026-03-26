package org.howread.app.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.howread.common.response.ApiResponse;
import org.howread.user.application.UserService;
import org.howread.user.application.dto.AdminUserResponse;
import org.howread.user.domain.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 관리자 전용 API.
 * SecurityConfig에서 /api/v1/admin/** 경로에 ADMIN 권한만 허용하도록 설정되어 있다.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    /** 전체 사용자 목록 조회 */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    /** 특정 사용자의 권한 변경 */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponse<Void>> changeUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeRoleRequest request) {
        userService.changeUserRole(userId, request.role());
        return ResponseEntity.ok(ApiResponse.success());
    }

    record ChangeRoleRequest(@NotNull UserRole role) {}
}
