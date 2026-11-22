package com.certsniper.auth.service;

import com.certsniper.auth.dto.PasswordChangeRequest;
import com.certsniper.auth.dto.PasswordResetRequest;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordService {

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
