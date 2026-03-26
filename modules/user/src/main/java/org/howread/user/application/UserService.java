package org.howread.user.application;

import lombok.RequiredArgsConstructor;
import org.howread.common.exception.BusinessException;
import org.howread.user.application.dto.*;
import org.howread.user.application.port.*;
import org.howread.user.domain.EmailVerification;
import org.howread.user.domain.RefreshToken;
import org.howread.user.domain.User;
import org.howread.user.domain.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;

/**
 * User 도메인의 핵심 Use Case를 조율하는 Application Service.
 *
 * 비즈니스 흐름을 조율하되, 개별 규칙은 도메인 엔티티 메서드에 위임한다.
 * Port 인터페이스를 통해 infra 구현체에 의존하여 기술 독립성을 유지한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final int CODE_LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtPort jwtPort;
    private final EmailPort emailPort;
    private final ProfileImagePort profileImagePort;
    private final NicknameGenerator nicknameGenerator;
    private final PasswordEncoder passwordEncoder;

    /** 이메일 중복 확인. 이미 존재하면 BusinessException 발생. */
    public void checkEmailDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    /** 이메일 인증번호 발송. 기존 레코드를 모두 삭제하고 새 코드를 저장 후 이메일 발송. */
    @Transactional
    public void sendVerificationCode(String email) {
        emailVerificationRepository.deleteByEmail(email);
        String code = generateCode();
        EmailVerification ev = EmailVerification.create(email, code);
        emailVerificationRepository.save(ev);
        emailPort.sendVerificationCode(email, code);
    }

    /** 인증번호 검증. 만료 또는 코드 불일치 시 BusinessException 발생. */
    @Transactional
    public void verifyEmailCode(String email, String code) {
        EmailVerification ev = emailVerificationRepository.findLatestByEmail(email)
                .orElseThrow(() -> new BusinessException(UserErrorCode.EMAIL_NOT_VERIFIED));

        if (ev.isExpired()) {
            throw new BusinessException(UserErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (!ev.isCodeMatch(code)) {
            throw new BusinessException(UserErrorCode.VERIFICATION_CODE_INVALID);
        }
        ev.verify();
    }

    /**
     * 회원가입.
     * 이메일 인증 완료 여부 확인 → 닉네임 생성 → 사용자 저장 → 토큰 발급.
     */
    @Transactional
    public TokenResponse register(String email, String password) {
        EmailVerification ev = emailVerificationRepository.findLatestByEmail(email)
                .orElseThrow(() -> new BusinessException(UserErrorCode.EMAIL_NOT_VERIFIED));

        if (!ev.isVerified()) {
            throw new BusinessException(UserErrorCode.EMAIL_NOT_VERIFIED);
        }

        String nickname = nicknameGenerator.generate(userRepository::existsByNickname);
        String encodedPassword = passwordEncoder.encode(password);
        User user = User.create(email, encodedPassword, nickname);
        userRepository.save(user);
        emailVerificationRepository.deleteByEmail(email);

        return issueTokens(user);
    }

    /**
     * 로그인.
     * 탈퇴 계정 확인 → 비밀번호 검증 → 마지막 로그인 기록 → 토큰 발급.
     */
    @Transactional
    public TokenResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new BusinessException(UserErrorCode.DELETED_ACCOUNT);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(UserErrorCode.PASSWORD_INVALID);
        }

        user.recordLogin();
        return issueTokens(user);
    }

    /** RefreshToken으로 새 AccessToken 발급. */
    @Transactional
    public AccessTokenResponse refresh(String refreshToken) {
        jwtPort.validate(refreshToken);

        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(UserErrorCode.INVALID_TOKEN));

        if (stored.isExpired()) {
            throw new BusinessException(UserErrorCode.TOKEN_EXPIRED);
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtPort.generateAccessToken(user.getId(), user.getRole());
        return new AccessTokenResponse(newAccessToken);
    }

    /** 로그아웃. 해당 사용자의 모든 RefreshToken 삭제. */
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    /** 회원 탈퇴 (Soft Delete). RefreshToken도 즉시 삭제하여 재로그인을 차단. */
    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        user.delete();
        refreshTokenRepository.deleteByUserId(userId);
    }

    /** 내 프로필 조회. */
    public UserProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        String imageUrl = user.getProfileImageKey() != null
                ? profileImagePort.buildUrl(user.getProfileImageKey())
                : null;

        return new UserProfileResponse(
                user.getEmail(),
                user.getNickname(),
                imageUrl,
                user.getRole().name(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }

    /**
     * 비밀번호 변경.
     * 현재 비밀번호 검증 → 새 비밀번호 해시 → User.changePassword().
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(UserErrorCode.PASSWORD_INVALID);
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    /**
     * 닉네임 변경.
     * 중복 확인 → User.changeNickname().
     */
    @Transactional
    public void changeNickname(Long userId, ChangeNicknameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(UserErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        user.changeNickname(request.nickname());
    }

    /**
     * 프로필 이미지 변경.
     * 기존 이미지가 있으면 S3에서 삭제 → 새 이미지 업로드 → key 저장 → CloudFront URL 반환.
     */
    @Transactional
    public String changeProfileImage(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (user.getProfileImageKey() != null) {
            profileImagePort.delete(user.getProfileImageKey());
        }

        String key = profileImagePort.upload(userId, file);
        user.changeProfileImageKey(key);

        return profileImagePort.buildUrl(key);
    }

    /**
     * 비밀번호 재설정 코드 발송.
     * 존재하지 않는 이메일도 보안상 동일 응답 (사용자 존재 여부 노출 방지).
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        if (!userRepository.existsByEmail(request.email())) {
            return;
        }

        String code = generateCode();
        EmailVerification ev = EmailVerification.create(request.email(), code);
        emailVerificationRepository.save(ev);
        emailPort.sendPasswordResetCode(request.email(), code);
    }

    /**
     * 비밀번호 재설정.
     * 인증코드 검증 → 새 비밀번호 해시 → User.changePassword().
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        EmailVerification ev = emailVerificationRepository.findLatestByEmail(request.email())
                .orElseThrow(() -> new BusinessException(UserErrorCode.EMAIL_NOT_VERIFIED));

        if (ev.isExpired()) {
            throw new BusinessException(UserErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (!ev.isCodeMatch(request.code())) {
            throw new BusinessException(UserErrorCode.VERIFICATION_CODE_INVALID);
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    /** 관리자용: 전체 사용자 목록 조회 */
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new AdminUserResponse(
                        u.getId(),
                        u.getEmail(),
                        u.getNickname(),
                        u.getProfileImageKey() != null ? profileImagePort.buildUrl(u.getProfileImageKey()) : null,
                        u.getRole().name(),
                        u.getCreatedAt(),
                        u.getLastLoginAt()
                ))
                .toList();
    }

    /** 관리자용: 특정 사용자의 권한을 변경한다. */
    @Transactional
    public void changeUserRole(Long targetUserId, UserRole newRole) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        user.changeRole(newRole);
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtPort.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtPort.generateRefreshToken(user.getId());

        RefreshToken rt = RefreshToken.create(
                user.getId(),
                refreshToken,
                jwtPort.extractRefreshTokenExpiresAt(refreshToken)
        );
        refreshTokenRepository.save(rt);

        return new TokenResponse(accessToken, refreshToken);
    }

    private String generateCode() {
        return String.format("%0" + CODE_LENGTH + "d",
                SECURE_RANDOM.nextInt((int) Math.pow(10, CODE_LENGTH)));
    }

}
