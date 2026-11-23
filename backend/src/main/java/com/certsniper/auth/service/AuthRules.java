package com.certsniper.auth.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 이메일·비밀번호·닉네임 공통 규칙 (회원가입·비밀번호 재설정·변경에서 같이 사용).
 * 기준: 요구사항 정의서 6.2 이메일 규칙, 6-4 입력 길이 기준.
 */
public final class AuthRules {

    /** 가입 가능한 이메일 도메인 9개 (학교·회사 메일 제외) */
    public static final Set<String> ALLOWED_DOMAINS = Set.of(
            "naver.com", "daum.net", "hanmail.net", "kakao.com", "nate.com",
            "gmail.com", "outlook.com", "hotmail.com", "icloud.com");

    /** 아이디 부분: 영문·숫자·. _ - 만, 1~64자 */
    private static final Pattern LOCAL_PART = Pattern.compile("^[a-z0-9._-]{1,64}$");

    /** 8~20자, 영문 대문자·소문자·숫자·특수문자 각 1개 이상, 공백·한글 불가 */
    private static final Pattern PASSWORD = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])[\\x21-\\x7E]{8,20}$");

    public static final String EMAIL_MESSAGE = "올바른 이메일 형식으로 입력해 주세요.";
    public static final String PASSWORD_MESSAGE = "영문 대소문자, 숫자, 특수문자를 조합하여 입력해주세요.(8~20자)";

    /** 닉네임: 완성형 한글(가~힣)·영문·숫자만 (특수문자·이모지·공백 불가) */
    private static final Pattern NICKNAME_CHARS = Pattern.compile("^[가-힣A-Za-z0-9]+$");

    /** 한글 자음·모음 낱자 (ㄱ~ㅎ, ㅏ~ㅣ, 옛 자모 포함) */
    private static final Pattern HANGUL_JAMO = Pattern.compile("[\\u1100-\\u11FF\\u3131-\\u318E]");

    public static final String NICKNAME_REQUIRED_MESSAGE = "닉네임을 입력해 주세요.";
    public static final String NICKNAME_JAMO_MESSAGE = "자음·모음만 따로 사용할 수 없어요.";
    public static final String NICKNAME_CHARS_MESSAGE = "한글, 영문, 숫자만 사용할 수 있어요. (특수문자·이모지·공백 불가)";
    public static final String NICKNAME_LENGTH_MESSAGE = "닉네임은 2~10자로 입력해 주세요.";

    private AuthRules() {}

    /** 앞뒤 공백 제거 + 소문자 (비교·저장 기준) */
    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /** 정규화된 이메일이 규칙에 맞는지 */
    public static boolean isValidEmail(String email) {
        if (email == null || email.length() > 254) return false;
        int at = email.indexOf('@');
        if (at < 0 || at != email.lastIndexOf('@')) return false;
        String local = email.substring(0, at);
        String domain = email.substring(at + 1);
        if (!LOCAL_PART.matcher(local).matches()) return false;
        if (local.startsWith(".") || local.endsWith(".") || local.contains("..")) return false;
        return ALLOWED_DOMAINS.contains(domain);
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD.matcher(password).matches();
    }

    /** 앞뒤 공백 제거 */
    public static String normalizeNickname(String nickname) {
        return nickname == null ? null : nickname.trim();
    }

    /**
     * 닉네임 규칙 위반 문구 (통과하면 null). 앞뒤 공백은 지운 값으로 검사한다.
     * 1) 빈 값 2) 자음·모음 낱자 3) 한글·영문·숫자 외 문자 4) 2~10자
     */
    public static String nicknameError(String normalizedNickname) {
        if (normalizedNickname == null || normalizedNickname.isEmpty()) return NICKNAME_REQUIRED_MESSAGE;
        if (HANGUL_JAMO.matcher(normalizedNickname).find()) return NICKNAME_JAMO_MESSAGE;
        if (!NICKNAME_CHARS.matcher(normalizedNickname).matches()) return NICKNAME_CHARS_MESSAGE;
        if (normalizedNickname.length() < 2 || normalizedNickname.length() > 10) return NICKNAME_LENGTH_MESSAGE;
        return null;
    }

    public static boolean isValidNickname(String normalizedNickname) {
        return nicknameError(normalizedNickname) == null;
    }

    public static void requireValidEmail(String normalizedEmail) {
        if (!isValidEmail(normalizedEmail)) throw new BusinessException(ErrorCode.VALIDATION_ERROR, EMAIL_MESSAGE);
    }

    public static void requireValidPassword(String password) {
        if (!isValidPassword(password)) throw new BusinessException(ErrorCode.VALIDATION_ERROR, PASSWORD_MESSAGE);
    }

    public static void requireValidNickname(String normalizedNickname) {
        String message = nicknameError(normalizedNickname);
        if (message != null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, message);
        }
    }
}