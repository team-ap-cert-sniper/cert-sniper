package com.certsniper.auth.service;

import com.certsniper.auth.dto.LoginRequest;
import com.certsniper.auth.dto.LoginResponse;
import com.certsniper.auth.dto.NicknameCheckResponse;
import com.certsniper.auth.dto.RegisterRequest;
import com.certsniper.auth.dto.TokenRefreshRequest;
import com.certsniper.auth.repository.UserTokenRepository;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.option.service.OptionService;
import com.certsniper.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    /** 이메일 인증 용도: 회원가입 */
    public static final String PURPOSE_REGISTER = "REGISTER";

    private final UserTokenRepository userTokenRepository;
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final OptionService optionService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 (AUTH-01). 화면 검사와 별개로 서버에서 다시 검증한다.
     * 1) 이메일 규칙(허용 도메인 9개 포함) 2) 비밀번호 규칙 3) 닉네임 길이
     * 4) 이메일 인증 완료 여부 5) 대학·전공·학년 묶음
     * 6) 저장 직전 이메일·닉네임 중복 재확인(UserService.createUser) 후 BCrypt 해시로 저장.
     * 자동 로그인은 하지 않는다.
     */
    @Transactional
    public void register(RegisterRequest request) {
        String email = AuthRules.normalizeEmail(request.email());
        AuthRules.requireValidEmail(email);
        AuthRules.requireValidPassword(request.password());
        AuthRules.requireValidNickname(AuthRules.normalizeNickname(request.nickname()));

        emailVerificationService.checkVerified(email, PURPOSE_REGISTER);
        optionService.validateSchoolInfo(
                request.universityId(), request.collegeId(), request.majorId(), request.grade());

        userService.createUser(request, passwordEncoder.encode(request.password()));
    }

    /** 닉네임 중복 확인 (2~10자, 공백 제거 후 비교) */
    public NicknameCheckResponse checkNickname(String nickname) {
        String normalized = AuthRules.normalizeNickname(nickname);
        AuthRules.requireValidNickname(normalized);
        return new NicknameCheckResponse(normalized, userService.isNicknameAvailable(normalized));
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public LoginResponse refreshToken(TokenRefreshRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void logout(TokenRefreshRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** 탈퇴·비밀번호 변경·재설정·정지 시 모든 로그인 토큰 폐기 */
    @Transactional
    public void revokeAllTokens(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}