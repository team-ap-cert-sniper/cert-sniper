package com.certsniper.auth.controller;

import com.certsniper.auth.dto.EmailCodeRequest;
import com.certsniper.auth.dto.EmailVerifyRequest;
import com.certsniper.auth.dto.LoginRequest;
import com.certsniper.auth.dto.LoginResponse;
import com.certsniper.auth.dto.NicknameCheckResponse;
import com.certsniper.auth.dto.PasswordChangeRequest;
import com.certsniper.auth.dto.PasswordResetRequest;
import com.certsniper.auth.dto.RegisterRequest;
import com.certsniper.auth.dto.TokenRefreshRequest;
import com.certsniper.auth.service.AuthService;
import com.certsniper.auth.service.EmailVerificationService;
import com.certsniper.auth.service.PasswordService;
import com.certsniper.global.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "01. 회원 인증")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordService passwordService;

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
    }

    @Operation(summary = "닉네임 중복 확인")
    @GetMapping("/nickname-check")
    public NicknameCheckResponse checkNickname(@Parameter(description = "닉네임") @RequestParam(name = "nickname") String nickname) {
        return authService.checkNickname(nickname);
    }

    @Operation(summary = "이메일 인증코드 발송")
    @PostMapping("/email-code")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendEmailCode(@Valid @RequestBody EmailCodeRequest request) {
        emailVerificationService.sendEmailCode(request);
    }

    @Operation(summary = "이메일 인증코드 확인")
    @PostMapping("/email-verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmailCode(@Valid @RequestBody EmailVerifyRequest request) {
        emailVerificationService.verifyEmailCode(request);
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/token-refresh")
    public LoginResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return authService.refreshToken(request);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody TokenRefreshRequest request) {
        authService.logout(request);
    }

    @Operation(summary = "비밀번호 재설정 (로그인 전)")
    @PostMapping("/password-reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordService.resetPassword(request);
    }

    @Operation(summary = "비밀번호 변경 (마이페이지)")
    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @LoginUser Long userId,
            @Valid @RequestBody PasswordChangeRequest request) {
        passwordService.changePassword(userId, request);
    }
}
