package com.certsniper.auth.service;

import com.certsniper.auth.dto.LoginRequest;
import com.certsniper.auth.dto.LoginResponse;
import com.certsniper.auth.dto.NicknameCheckResponse;
import com.certsniper.auth.dto.RegisterRequest;
import com.certsniper.auth.dto.TokenRefreshRequest;
import com.certsniper.auth.repository.UserTokenRepository;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserTokenRepository userTokenRepository;

    @Transactional
    public void register(RegisterRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    public NicknameCheckResponse checkNickname(String nickname) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
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
