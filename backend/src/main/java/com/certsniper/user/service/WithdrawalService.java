package com.certsniper.user.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.user.dto.WithdrawRequest;
import com.certsniper.user.repository.AccountStatusHistoryRepository;
import com.certsniper.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WithdrawalService {

    private final UserRepository userRepository;
    private final AccountStatusHistoryRepository accountStatusHistoryRepository;

    @Transactional
    public void requestWithdrawal(Long userId, WithdrawRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void cancelWithdrawal(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** 7일 유예가 끝난 탈퇴 계정 정리 (스케줄러) */
    @Transactional
    public void deleteExpiredWithdrawals() {
    }
}
