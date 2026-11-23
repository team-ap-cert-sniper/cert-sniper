package com.certsniper.user.service;

import com.certsniper.auth.service.AuthService;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import com.certsniper.user.dto.WithdrawRequest;
import com.certsniper.user.entity.AccountStatusHistory;
import com.certsniper.user.entity.User;
import com.certsniper.user.repository.AccountStatusHistoryRepository;
import com.certsniper.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WithdrawalService {

    private final UserRepository userRepository;
    private final AccountStatusHistoryRepository accountStatusHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    /** 현재 비밀번호 확인 → 탈퇴 대기(7일 유예) → 이력 → 모든 기기 로그아웃 */
    @Transactional
    public void requestWithdrawal(Long userId, WithdrawRequest request) {
        User u = findUser(userId);
        if ("WITHDRAWAL_PENDING".equals(u.getStatus())) {
            throw new BusinessException(ErrorCode.DUPLICATE);
        }
        if (!"ACTIVE".equals(u.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (!passwordEncoder.matches(request.password(), u.getPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "비밀번호가 일치하지 않습니다.");
        }
        u.requestWithdrawal(DateUtils.now());
        accountStatusHistoryRepository.save(AccountStatusHistory.of(
                userId, "WITHDRAW_REQUEST", "ACTIVE", "WITHDRAWAL_PENDING", null, null, null));
        authService.revokeAllTokens(userId);
    }

    @Transactional
    public void cancelWithdrawal(Long userId) {
        User u = findUser(userId);
        if (!"WITHDRAWAL_PENDING".equals(u.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        u.activate();
        accountStatusHistoryRepository.save(AccountStatusHistory.of(
                userId, "WITHDRAW_CANCEL", "WITHDRAWAL_PENDING", "ACTIVE", null, null, null));
    }

    /** 7일 유예가 끝난 탈퇴 계정 정리 (스케줄러) */
    @Transactional
    public void deleteExpiredWithdrawals() {
        // 비식별 규칙(email·nickname UNIQUE, name 2~4자 CHECK를 어떤 값으로 채울지) 팀 결정 전까지 보류 (kiwan.md Q9)
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }
}
