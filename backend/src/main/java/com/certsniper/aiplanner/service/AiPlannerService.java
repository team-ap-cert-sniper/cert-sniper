package com.certsniper.aiplanner.service;

import com.certsniper.aiplanner.dto.AiDraft;
import com.certsniper.aiplanner.dto.AiPlanResponse;
import com.certsniper.aiplanner.dto.SurveyRequest;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.planner.service.PlannerService;
import com.certsniper.qualification.dto.QualificationDetailResponse;
import com.certsniper.qualification.service.QualificationService;
import com.certsniper.targetexam.service.TargetExamService;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * AI 플래너 생성 (하이브리드 방식).
 * 설문 검사 → AI 초안(일정바·주제) → 서버가 날짜·일일 항목 계산 → 최종 검사 → 목표 자격증 + 일정 저장.
 * AI를 기다리는 동안 DB 연결을 잡고 있지 않도록, 저장할 때만 트랜잭션을 연다.
 */
@Service
@RequiredArgsConstructor
public class AiPlannerService {

    private final QualificationService qualificationService;
    private final TargetExamService targetExamService;
    private final PlannerService plannerService;
    private final QwenClient qwenClient;
    private final AiPlanValidator aiPlanValidator;
    private final DailyPointPlanner dailyPointPlanner;
    private final TransactionTemplate transactionTemplate;

    /** 같은 회원이 생성 버튼을 여러 번 눌러도 AI는 한 번만 돌게 막는다 */
    private final Set<Long> inProgress = ConcurrentHashMap.newKeySet();

    public AiPlanResponse createAiPlan(Long userId, SurveyRequest request) {
        if (!inProgress.add(userId)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "이미 플래너를 만드는 중입니다. 잠시만 기다려 주세요.");
        }
        try {
            return generateAndSave(userId, request);
        } finally {
            inProgress.remove(userId);
        }
    }

    private AiPlanResponse generateAndSave(Long userId, SurveyRequest request) {
        // 1. AI 부르기 전 검사 (날짜, 자격증, 회차·시험일, 6개 제한)
        aiPlanValidator.validateSurvey(request);
        QualificationDetailResponse.QualificationInfo info = qualificationService.getInfo(request.jmcd());
        targetExamService.validateBeforeAiPlan(userId, info.jmcd(), request.examScheduleId(), request.examDate());

        // 2. AI 초안 받기
        int totalDays = (int) ChronoUnit.DAYS.between(request.startDate(), request.examDate());
        int studyDayCount = (int) request.startDate().datesUntil(request.examDate())
                .filter(day -> request.studyDays().contains(day.getDayOfWeek()))
                .count();
        String prompt = QwenClient.buildPrompt(info.jmfldnm(), info.gradeLevel(), info.examSubjects(),
                request, totalDays, studyDayCount);
        AiDraft draft = aiPlanValidator.sanitize(qwenClient.requestDraft(prompt));

        // 3. 서버가 날짜·일일 항목 계산 후 검사
        AiPlanResponse plan = dailyPointPlanner.expand(info.jmcd(), request, draft);
        aiPlanValidator.validatePlan(plan);

        // 4. 목표 자격증과 일정을 한 번에 저장 (중간에 실패하면 둘 다 취소)
        transactionTemplate.executeWithoutResult(status -> {
            Long targetExamId = targetExamService.getOrCreateTargetExam(
                    userId, info.jmcd(), request.examScheduleId(), request.examDate());
            plannerService.replaceAiSchedule(userId, targetExamId, plan);
        });
        return plan;
    }
}
