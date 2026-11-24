package com.certsniper.auth.service;

import com.certsniper.auth.dto.EmailCodeRequest;
import com.certsniper.auth.dto.EmailVerifyRequest;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.user.service.UserService;
import java.security.SecureRandom;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 이메일 인증 (AUTH-01·05·06). 6자리 코드, 3분 유효, 5회 틀리면 무효, 하루 발송 횟수 제한.
 * 코드는 DB에 저장하지 않고 EmailCodeStore(서버 메모리)에 둔다.
 */
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    public static final String PURPOSE_REGISTER = "REGISTER";
    public static final String PURPOSE_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String PURPOSE_PASSWORD_CHANGE = "PASSWORD_CHANGE";
    private static final Set<String> PURPOSES = Set.of(PURPOSE_REGISTER, PURPOSE_PASSWORD_RESET, PURPOSE_PASSWORD_CHANGE);

    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailCodeStore codeStore;
    private final MailService mailService;
    private final UserService userService;

    /** 인증코드 발송 */
    public void sendEmailCode(EmailCodeRequest request) {
        String purpose = requirePurpose(request.purpose());
        String email = AuthRules.normalizeEmail(request.email());
        AuthRules.requireValidEmail(email);

                // 탈퇴 확정 계정은 미가입과 같게 본다 (회원가입은 막고, 비밀번호 재설정은 인증번호를 보내지 않음)
        var found = userService.findByEmail(email);
        boolean registered = found != null && !"WITHDRAWN".equals(found.getStatus());
        if (PURPOSE_REGISTER.equals(purpose) && registered) {
            throw new BusinessException(ErrorCode.DUPLICATE, "이미 가입된 이메일입니다.");
        }
        if (!PURPOSE_REGISTER.equals(purpose) && !registered) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "가입되지 않은 이메일입니다.");
        }

        String key = key(purpose, email);
        if (!codeStore.canSend(key)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "오늘 인증번호 발송 횟수를 모두 사용했어요. 내일 다시 시도해 주세요.");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        codeStore.saveCode(key, code);
        try {
            mailService.sendVerificationCode(email, code);
        } catch (RuntimeException e) {
            codeStore.cancelCode(key);
            throw e;
        }
    }

    /** 인증코드 확인 */
    public void verifyEmailCode(EmailVerifyRequest request) {
        String purpose = requirePurpose(request.purpose());
        String email = AuthRules.normalizeEmail(request.email());
        String code = request.code() == null ? "" : request.code().trim();

        switch (codeStore.verify(key(purpose, email), code)) {
            case OK -> { }
            case NO_CODE -> throw new BusinessException(ErrorCode.VALIDATION_ERROR, "인증번호를 먼저 요청해 주세요.");
            case EXPIRED -> throw new BusinessException(ErrorCode.VALIDATION_ERROR, "인증 시간이 지났습니다. 인증번호를 다시 받아 주세요.");
            case MISMATCH -> throw new BusinessException(ErrorCode.VALIDATION_ERROR, "인증번호가 일치하지 않습니다.");
            case TOO_MANY_FAILURES -> throw new BusinessException(ErrorCode.VALIDATION_ERROR, "인증번호를 5회 잘못 입력했습니다. 인증번호를 다시 받아 주세요.");
        }
    }

    /** 가입·재설정·변경 전에 인증 완료 여부 확인 */
    public void checkVerified(String email, String purpose) {
        if (!codeStore.isVerified(key(requirePurpose(purpose), AuthRules.normalizeEmail(email)))) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "이메일 인증을 완료해 주세요.");
        }
    }

    /** 가입·재설정을 마친 뒤 인증 완료 상태를 지움 (같은 인증으로 두 번 쓰지 못하게) */
    public void clearVerified(String email, String purpose) {
        codeStore.clearVerified(key(requirePurpose(purpose), AuthRules.normalizeEmail(email)));
    }

    private static String requirePurpose(String purpose) {
        if (purpose == null || !PURPOSES.contains(purpose)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "인증 용도가 올바르지 않습니다.");
        }
        return purpose;
    }

    private static String key(String purpose, String email) {
        return purpose + ":" + email;
    }
}