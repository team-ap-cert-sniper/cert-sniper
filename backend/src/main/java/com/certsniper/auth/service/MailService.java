package com.certsniper.auth.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

/** 인증 메일 발송 */
@Service
public class MailService {

    public void sendVerificationCode(String email, String code) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
