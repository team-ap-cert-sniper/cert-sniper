package com.certsniper.auth.service;

import com.certsniper.auth.dto.EmailCodeRequest;
import com.certsniper.auth.dto.EmailVerifyRequest;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    @Transactional
    public void sendEmailCode(EmailCodeRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void verifyEmailCode(EmailVerifyRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** 가입·재설정·변경 전에 인증 완료 여부 확인 */
    public void checkVerified(String email, String purpose) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
