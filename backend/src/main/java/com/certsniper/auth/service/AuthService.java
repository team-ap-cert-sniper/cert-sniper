package com.certsniper.auth.service;

import com.certsniper.auth.dto.LoginRequest;
import com.certsniper.auth.dto.LoginResponse;
import com.certsniper.auth.dto.NicknameCheckResponse;
import com.certsniper.auth.dto.RegisterRequest;
import com.certsniper.auth.dto.TokenRefreshRequest;
import com.certsniper.auth.entity.UserToken;
import com.certsniper.auth.repository.UserTokenRepository;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.security.JwtTokenProvider;
import com.certsniper.global.util.DateUtils;
import com.certsniper.option.service.OptionService;
import com.certsniper.user.entity.User;
import com.certsniper.user.service.UserService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
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
    private final JwtTokenProvider jwtTokenProvider;

    private static final String STATUS_SUSPENDED = "SUSPENDED";
    private static final String STATUS_WITHDRAWN = "WITHDRAWN";
    private static final DateTimeFormatter SUSPEND_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

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

    /**
     * 로그인 (AUTH-02).
     * 이메일이 없거나 비밀번호가 틀리거나 탈퇴 확정 계정이면 같은 문구(LOGIN_FAILED)로 막는다.
     * 정지 기간이 끝났으면 ACTIVE로 되돌린 뒤, 아직 정지 중이면 정지 종료 일시를 안내한다.
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userService.findByEmail(AuthRules.normalizeEmail(request.email()));
        if (user == null
                || STATUS_WITHDRAWN.equals(user.getStatus())
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        userService.releaseExpiredSuspension(user.getId());
        requireNotSuspended(user);
        return issueTokens(user, UUID.randomUUID().toString());
    }

    /**
     * 토큰 재발급. Refresh Token은 한 번 쓰면 폐기하고 새 것을 준다(같은 로그인 = 같은 family).
     * 없거나 만료된 토큰은 INVALID_TOKEN → 화면은 로그아웃 처리.
     */
    @Transactional
    public LoginResponse refreshToken(TokenRefreshRequest request) {
        UserToken saved = userTokenRepository.findByTokenHash(jwtTokenProvider.hash(request.refreshToken()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        userTokenRepository.delete(saved);
        if (saved.isExpired(DateUtils.now())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        User user = userService.getUser(saved.getUserId());
        if (STATUS_WITHDRAWN.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        userService.releaseExpiredSuspension(user.getId());
        requireNotSuspended(user);
        return issueTokens(user, saved.getTokenFamily());
    }

    /** 로그아웃: 이 기기의 Refresh Token만 폐기 (이미 없으면 그냥 성공) */
    @Transactional
    public void logout(TokenRefreshRequest request) {
        userTokenRepository.findByTokenHash(jwtTokenProvider.hash(request.refreshToken()))
                .ifPresent(userTokenRepository::delete);
    }

    /** 탈퇴·비밀번호 변경·재설정·정지 시 모든 로그인 토큰 폐기 */
    @Transactional
    public void revokeAllTokens(Long userId) {
        userTokenRepository.deleteByUserId(userId);
    }

    private void requireNotSuspended(User user) {
        if (STATUS_SUSPENDED.equals(user.getStatus())) {
            LocalDateTime until = user.getSuspendedUntil();
            String message = until == null
                    ? "정지된 계정입니다."
                    : "정지된 계정입니다. " + until.format(SUSPEND_FORMAT) + "까지 로그인할 수 없습니다.";
            throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED, message);
        }
    }

    private LoginResponse issueTokens(User user, String tokenFamily) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken();
        userTokenRepository.save(UserToken.issue(
                user.getId(),
                jwtTokenProvider.hash(refreshToken),
                tokenFamily,
                DateUtils.now().plus(jwtTokenProvider.getRefreshTokenValidity())));
        return new LoginResponse(accessToken, refreshToken, user.getId(), user.getNickname(), user.getRole());
    }
}