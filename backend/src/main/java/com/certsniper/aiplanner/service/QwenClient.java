package com.certsniper.aiplanner.service;

import com.certsniper.aiplanner.dto.AiDraft;
import com.certsniper.aiplanner.dto.SurveyRequest;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Qwen2.5(Ollama) 호출·프롬프트. AI에게는 일정바 제목·대략 일수·주제만 받는다 (날짜는 서버가 계산) */
@Component
public class QwenClient {

    static final String SYSTEM_PROMPT = "너는 한국 국가자격증 시험 학습 계획 전문가다. 반드시 지정된 JSON 형식으로만 답한다.";
    /** 시험 과목 설명이 너무 길면 잘라서 보낸다 (프롬프트가 길수록 느려짐) */
    static final int MAX_SUBJECTS_LENGTH = 300;

    /** Ollama에 "이 모양의 JSON으로만 답해" 라고 알려 주는 형식 */
    static final Map<String, Object> DRAFT_SCHEMA = Map.of(
            "type", "object",
            "required", List.of("bars"),
            "properties", Map.of("bars", Map.of(
                    "type", "array",
                    "items", Map.of(
                            "type", "object",
                            "required", List.of("title", "days", "topics"),
                            "properties", Map.of(
                                    "title", Map.of("type", "string"),
                                    "days", Map.of("type", "integer"),
                                    "topics", Map.of("type", "array", "items", Map.of("type", "string")))))));

    private final RestClient aiRestClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public QwenClient(RestClient aiRestClient, ObjectMapper objectMapper, @Value("${app.ai.model}") String model) {
        this.aiRestClient = aiRestClient;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    /** 프롬프트를 보내고 AI 초안을 받는다. 연결 실패·시간 초과·형식 오류는 500 */
    public AiDraft requestDraft(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "stream", false,
                "format", DRAFT_SCHEMA,
                "options", Map.of("temperature", 0.3),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", prompt)));
        JsonNode response;
        try {
            response = aiRestClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "AI 서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
        String content = response == null ? "" : response.path("message").path("content").asText("");
        return parseDraft(objectMapper, content);
    }

    /** AI가 준 글자를 AiDraft로 바꾼다. ```json 같은 감싸는 표시가 있으면 벗겨 낸다 */
    static AiDraft parseDraft(ObjectMapper objectMapper, String content) {
        String json = content == null ? "" : content.strip();
        if (json.startsWith("```")) {
            json = json.replaceFirst("^```(json)?", "").replaceFirst("```$", "").strip();
        }
        try {
            AiDraft draft = objectMapper.readValue(json, AiDraft.class);
            if (draft.bars() == null || draft.bars().isEmpty()) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "AI가 학습 계획을 만들지 못했습니다. 다시 시도해 주세요.");
            }
            return draft;
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "AI 응답을 해석하지 못했습니다. 다시 시도해 주세요.");
        }
    }

    /** 설문 내용으로 질문(프롬프트)을 만든다 */
    static String buildPrompt(String qualificationName, String gradeLevel, String examSubjects,
                              SurveyRequest survey, int totalDays, int studyDayCount) {
        String subjects = examSubjects == null || examSubjects.isBlank() ? "정보 없음"
                : examSubjects.length() > MAX_SUBJECTS_LENGTH ? examSubjects.substring(0, MAX_SUBJECTS_LENGTH)
                : examSubjects;
        return """
                다음 조건으로 시험 공부 계획의 큰 단계(일정바)를 만들어 줘.
                - 자격증: %s%s
                - 시험 과목: %s
                - 공부 기간: %d일 (이 중 실제로 공부하는 날 %d일)
                - 하루 공부 시간: %d시간
                - 현재 수준: %s
                - 비율: 이론 %d%% / 문제 풀이 %d%%

                규칙:
                1. 일정바는 3~6개, 공부하는 순서대로 쓴다.
                2. 앞쪽은 과목별 이론, 뒤쪽은 문제 풀이, 마지막은 기출·모의고사로 마무리한다.
                3. days는 그 일정바에 쓸 대략적인 일수(정수)다. 이론과 문제 풀이 비율을 지켜서 나눈다.
                4. title은 20자 이내, topics는 일정바마다 2~8개이고 각각 15자 이내의 구체적인 공부 주제다.
                5. 날짜는 쓰지 않는다. JSON 말고 다른 설명은 쓰지 않는다.
                6. 모든 글자는 한국어로 쓴다. 한자나 중국어, 영어 문장은 쓰지 않는다.
                """.formatted(
                qualificationName,
                gradeLevel == null || gradeLevel.isBlank() ? "" : " (" + gradeLevel + ")",
                subjects, totalDays, studyDayCount, survey.dailyHours(), survey.baseLevel(),
                survey.theoryRatio(), 100 - survey.theoryRatio());
    }
}
