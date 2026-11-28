package com.certsniper.aiplanner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.certsniper.aiplanner.dto.AiDraft;
import com.certsniper.aiplanner.dto.SurveyRequest;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class QwenClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String JSON = """
            {"bars":[{"title":"1과목 이론","days":8,"topics":["엔터티","속성"]},
                     {"title":"기출 풀이","days":4,"topics":["기출 1회"]}]}
            """;

    @Test
    void AI_응답_JSON을_초안으로_바꾼다() {
        AiDraft draft = QwenClient.parseDraft(objectMapper, JSON);
        assertEquals(2, draft.bars().size());
        assertEquals("1과목 이론", draft.bars().get(0).title());
        assertEquals(8, draft.bars().get(0).days());
        assertEquals(List.of("엔터티", "속성"), draft.bars().get(0).topics());
    }

    @Test
    void 코드블록으로_감싸져_와도_읽는다() {
        AiDraft draft = QwenClient.parseDraft(objectMapper, "```json\n" + JSON + "\n```");
        assertEquals(2, draft.bars().size());
    }

    @Test
    void JSON이_아니면_500() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> QwenClient.parseDraft(objectMapper, "죄송합니다. 계획을 만들 수 없습니다."));
        assertEquals(ErrorCode.INTERNAL_ERROR, e.getErrorCode());
    }

    @Test
    void 일정바가_비어_있으면_500() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> QwenClient.parseDraft(objectMapper, "{\"bars\":[]}"));
        assertEquals(ErrorCode.INTERNAL_ERROR, e.getErrorCode());
    }

    @Test
    void 질문에_설문_내용이_들어간다() {
        SurveyRequest survey = new SurveyRequest("2290", 7L, LocalDate.of(2026, 12, 21), LocalDate.of(2026, 12, 1),
                "처음 공부", 2, List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), 60);

        String prompt = QwenClient.buildPrompt("SQL개발자", "", "데이터 모델링의 이해, SQL 기본 및 활용",
                survey, 20, 6);

        assertTrue(prompt.contains("자격증: SQL개발자"));
        assertTrue(prompt.contains("데이터 모델링의 이해"));
        assertTrue(prompt.contains("공부 기간: 20일 (이 중 실제로 공부하는 날 6일)"));
        assertTrue(prompt.contains("이론 60% / 문제 풀이 40%"));
        assertTrue(prompt.contains("처음 공부"));
    }
}
