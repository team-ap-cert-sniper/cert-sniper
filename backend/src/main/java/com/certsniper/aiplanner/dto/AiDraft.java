package com.certsniper.aiplanner.dto;

import java.util.List;

/** AI(Qwen) 응답 초안: 일정바 제목·대략 일수·공부 주제만. 실제 날짜와 일일 항목은 서버가 계산한다 */
public record AiDraft(List<Bar> bars) {

    public record Bar(
            String title,
            int days,
            List<String> topics
    ) {
    }
}
