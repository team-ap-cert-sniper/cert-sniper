package com.certsniper.auth.service;

import com.certsniper.auth.dto.PasswordChangeRequest;
import com.certsniper.auth.dto.PasswordResetRequest;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.user.entity.User;
import com.certsniper.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordService {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 재설정 (AUTH-05, 로그인 전).
     * 1) 새 비밀번호 규칙 2) PASSWORD_RESET 이메일 인증 완료 여부 3) 가입된 이메일인지
     * → BCrypt로 저장하고, 다른 기기의 로그인은 모두 끊는다.
     */
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        String email = AuthRules.normalizeEmail(request.email());
        AuthRules.requireValidPassword(request.newPassword());
        emailVerificationService.checkVerified(email, EmailVerificationService.PURPOSE_PASSWORD_RESET);

        User user = userService.findByEmail(email);
        if (user == null || "WITHDRAWN".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "가입되지 않은 이메일입니다.");
        }
        userService.updatePassword(user.getId(), passwordEncoder.encode(request.newPassword()));
        authService.revokeAllTokens(user.getId());
        emailVerificationService.clearVerified(email, EmailVerificationService.PURPOSE_PASSWORD_RESET);
    }

    /** 비밀번호 변경 (AUTH-06, 마이페이지 · 담당: 최기완) */
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}