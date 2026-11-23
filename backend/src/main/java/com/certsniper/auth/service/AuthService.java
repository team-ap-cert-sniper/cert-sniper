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
     * 1) 이메일 규칙(허용 도메인 9개 포함) 2) 비밀번호 규칙 3) 닉네임 규칙
     * 4) 이메일 인증 완료 여부 5) 대학·전공·학년 묶음
     * 6) 저장 직전 이메일·닉네임 중복 재확인 후 BCrypt 해시로 저장.
     * 자동 로그인은 하지 않는다.
     */
    @Transactional
    public void register(RegisterRequest request) {
        String email = AuthRules.normalizeEmail(request.email());
        String nickname = AuthRules.normalizeNickname(request.nickname());
        AuthRules.requireValidEmail(email);
        AuthRules.requireValidPassword(request.password());
        AuthRules.requireValidNickname(nickname);

        emailVerificationService.checkVerified(email, PURPOSE_REGISTER);
        optionService.validateSchoolInfo(
                request.universityId(), request.collegeId(), request.majorId(), request.grade());

        if (userService.findByEmail(email) != null) {
            throw new BusinessException(ErrorCode.DUPLICATE, "이미 가입된 이메일입니다.");
        }
        if (!userService.isNicknameAvailable(nickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE, "이미 사용 중인 닉네임입니다.");
        }

        // 소문자로 바꾼 이메일·앞뒤 공백을 뺀 닉네임으로 저장
        RegisterRequest normalized = new RegisterRequest(
                email, request.password(), request.name().trim(), nickname,
                request.universityId(), request.collegeId(), request.majorId(), request.grade(),
                request.ownedJmcds(), request.ownedCertNames());
        userService.createUser(normalized, passwordEncoder.encode(request.password()));
        emailVerificationService.clearVerified(email, PURPOSE_REGISTER);
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