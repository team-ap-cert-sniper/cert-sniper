package com.certsniper.support.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 1:1 문의 유형 (inquiries.inquiry_type CHECK 와 같은 값) */
@Getter
@RequiredArgsConstructor
public enum InquiryType {

    ACCOUNT("계정/로그인/회원정보 관련"),
    PLANNER("캘린더 및 학습 플래너 오류/기능 문의"),
    SUGGESTION("서비스 기능 제안 및 개선 의견"),
    CERT_DATA("자격증 정보/일정/통계 오류 제보"),
    ETC("기타 문의");

    private final String label;
}
