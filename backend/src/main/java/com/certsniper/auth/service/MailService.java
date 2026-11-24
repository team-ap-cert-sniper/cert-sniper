package com.certsniper.auth.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** 인증 메일 발송 (Gmail SMTP, 계정은 .env의 MAIL_USERNAME·MAIL_PASSWORD) */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    public void sendVerificationCode(String email, String code) {
        if (from == null || from.isBlank()) {
            // .env에 메일 계정이 없으면 개발용으로 서버 로그에만 남김 (운영에서는 반드시 계정 설정)
            log.warn("[메일 계정 미설정] {} 인증번호: {}", email, code);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("[저격증] 이메일 인증번호 안내");
        message.setText("""
                안녕하세요, 저격증입니다.

                아래 인증번호 6자리를 화면에 입력해 주세요.

                인증번호: %s

                인증번호는 3분 동안 유효합니다.
                본인이 요청하지 않았다면 이 메일을 무시해 주세요.
                """.formatted(code));
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("인증 메일 발송 실패: {}", email, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "인증 메일을 보내지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }
}