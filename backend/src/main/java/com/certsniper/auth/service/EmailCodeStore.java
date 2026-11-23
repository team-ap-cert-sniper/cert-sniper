package com.certsniper.auth.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** 인증코드·인증 완료·실패 횟수 메모리 저장 (키 = 용도:이메일, DB 저장 없음) */
@Component
public class EmailCodeStore {

    /** 코드 유효 시간 3분 */
    static final Duration CODE_TTL = Duration.ofMinutes(3);
    /** 코드 불일치 허용 횟수 (5회 틀리면 코드 무효 → 재발송) */
    static final int MAX_FAILURES = 5;
    /** 같은 이메일·용도로 하루에 보낼 수 있는 횟수 */
    static final int DAILY_SEND_LIMIT = 10;
    /** 인증 완료 상태 유지 시간 (이 안에 가입·재설정을 마쳐야 함) */
    static final Duration VERIFIED_TTL = Duration.ofMinutes(30);

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final Map<String, CodeEntry> codes = new ConcurrentHashMap<>();
    private final Map<String, Instant> verified = new ConcurrentHashMap<>();
    private final Map<String, DailyCount> sendCounts = new ConcurrentHashMap<>();
    private final Clock clock;

    public EmailCodeStore() {
        this(Clock.systemUTC());
    }

    EmailCodeStore(Clock clock) {
        this.clock = clock;
    }

    /** 검증 결과 */
    public enum VerifyResult { OK, NO_CODE, EXPIRED, MISMATCH, TOO_MANY_FAILURES }

    /** 오늘 발송 횟수가 남았는지 */
    public synchronized boolean canSend(String key) {
        DailyCount count = sendCounts.get(key);
        return count == null || !count.date().equals(today()) || count.count() < DAILY_SEND_LIMIT;
    }

    /** 새 코드 저장 (이전 코드·인증 완료 상태는 지움) */
    public synchronized void saveCode(String key, String code) {
        codes.put(key, new CodeEntry(code, now().plus(CODE_TTL), 0));
        verified.remove(key);
        LocalDate today = today();
        DailyCount count = sendCounts.get(key);
        int next = (count == null || !count.date().equals(today)) ? 1 : count.count() + 1;
        sendCounts.put(key, new DailyCount(today, next));
    }

    /** 메일 발송 실패 시 저장한 코드를 되돌림 (발송 횟수도 1 감소) */
    public synchronized void cancelCode(String key) {
        codes.remove(key);
        DailyCount count = sendCounts.get(key);
        if (count != null && count.count() > 0) {
            sendCounts.put(key, new DailyCount(count.date(), count.count() - 1));
        }
    }

    /** 코드 확인. 맞으면 인증 완료로 바꾸고 코드는 지움 */
    public synchronized VerifyResult verify(String key, String code) {
        CodeEntry entry = codes.get(key);
        if (entry == null) {
            return VerifyResult.NO_CODE;
        }
        if (now().isAfter(entry.expiresAt())) {
            codes.remove(key);
            return VerifyResult.EXPIRED;
        }
        if (!entry.code().equals(code)) {
            int failures = entry.failures() + 1;
            if (failures >= MAX_FAILURES) {
                codes.remove(key);
                return VerifyResult.TOO_MANY_FAILURES;
            }
            codes.put(key, new CodeEntry(entry.code(), entry.expiresAt(), failures));
            return VerifyResult.MISMATCH;
        }
        codes.remove(key);
        verified.put(key, now().plus(VERIFIED_TTL));
        return VerifyResult.OK;
    }

    /** 인증 완료 상태인지 (유지 시간 안) */
    public synchronized boolean isVerified(String key) {
        Instant until = verified.get(key);
        if (until == null) {
            return false;
        }
        if (now().isAfter(until)) {
            verified.remove(key);
            return false;
        }
        return true;
    }

    /** 가입·재설정을 마친 뒤 인증 완료 상태를 지움 (한 번만 사용) */
    public synchronized void clearVerified(String key) {
        verified.remove(key);
    }

    private Instant now() {
        return clock.instant();
    }

    private LocalDate today() {
        return LocalDate.ofInstant(now(), SEOUL);
    }

    private record CodeEntry(String code, Instant expiresAt, int failures) {}

    private record DailyCount(LocalDate date, int count) {}
}