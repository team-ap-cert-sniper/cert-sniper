package com.certsniper.user.scheduler;

import com.certsniper.user.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 7일 유예가 끝난 탈퇴 계정 정리 (매일 04:00) */
@Component
@RequiredArgsConstructor
public class WithdrawalCleanupScheduler {

    private final WithdrawalService withdrawalService;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void run() {
        withdrawalService.deleteExpiredWithdrawals();
    }
}
