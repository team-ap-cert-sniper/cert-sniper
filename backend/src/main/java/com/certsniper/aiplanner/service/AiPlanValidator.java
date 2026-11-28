package com.certsniper.aiplanner.service;

import com.certsniper.aiplanner.dto.AiDraft;
import com.certsniper.aiplanner.dto.AiPlanResponse;
import com.certsniper.aiplanner.dto.SurveyRequest;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * AI 플래너 검사.
 * ① AI를 부르기 전에 설문 날짜 검사 (잘못된 요청에 AI 시간을 쓰지 않게)
 * ② AI 초안 다듬기 (빈 제목·빈 주제·너무 많은 항목 정리)
 * ③ 저장 전에 최종 플랜 검사 (기간을 빈틈·겹침 없이 덮는지, 날마다 일일 항목이 1개씩인지)
 */
@Component
public class AiPlanValidator {

    static final int MAX_PLAN_DAYS = 365;
    static final int MAX_BARS = 8;
    static final int MAX_TOPICS = 10;

    /** ① 설문 날짜 검사: 시작일은 오늘 이후, 시험일보다 앞, 기간은 1년 이내, 기간 안에 공부 가능 요일이 하루 이상 */
    public void validateSurvey(SurveyRequest survey) {
        LocalDate start = survey.startDate();
        LocalDate lastDay = survey.examDate().minusDays(1);
        if (start.isBefore(DateUtils.today())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "공부 시작일은 오늘 이후로 선택해 주세요.");
        }
        if (start.isAfter(lastDay)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "공부 시작일은 시험일보다 앞이어야 합니다.");
        }
        if (ChronoUnit.DAYS.between(start, lastDay) + 1 > MAX_PLAN_DAYS) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "학습 기간은 최대 1년까지 만들 수 있습니다.");
        }
        boolean hasStudyDay = start.datesUntil(survey.examDate())
                .anyMatch(day -> survey.studyDays().contains(day.getDayOfWeek()));
        if (!hasStudyDay) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "시작일부터 시험 전날까지 공부 가능 요일이 하루도 없습니다.");
        }
    }

    /** ② AI 초안 다듬기: 일정바 최대 8개, 빈 제목은 "n단계 학습", 주제는 빈 것을 빼고 최대 10개 */
    public AiDraft sanitize(AiDraft draft) {
        List<AiDraft.Bar> bars = new ArrayList<>();
        for (AiDraft.Bar bar : draft.bars()) {
            if (bars.size() == MAX_BARS) {
                break;
            }
            String title = bar.title() == null || bar.title().isBlank()
                    ? (bars.size() + 1) + "단계 학습" : bar.title().strip();
            List<String> topics = bar.topics() == null ? List.of() : bar.topics().stream()
                    .filter(topic -> topic != null && !topic.isBlank())
                    .map(String::strip)
                    .limit(MAX_TOPICS)
                    .toList();
            bars.add(new AiDraft.Bar(title, bar.days(), topics));
        }
        return new AiDraft(bars);
    }

    /** ③ 최종 플랜 검사. 서버 계산 결과라 정상이라면 항상 통과하고, 실패하면 서버 쪽 오류(500) */
    public void validatePlan(AiPlanResponse plan) {
        LocalDate start = plan.meta().startDate();
        LocalDate lastDay = plan.meta().examDate().minusDays(1);
        List<AiPlanResponse.TimelineBar> bars = plan.timelineBars();
        if (bars.isEmpty() || !bars.get(0).startDate().equals(start)
                || !bars.get(bars.size() - 1).endDate().equals(lastDay)) {
            throw planError();
        }
        for (int i = 1; i < bars.size(); i++) {
            if (!bars.get(i).startDate().equals(bars.get(i - 1).endDate().plusDays(1))) {
                throw planError();
            }
        }
        List<AiPlanResponse.DailyPoint> points = plan.dailyPoints();
        if (points.size() != plan.meta().totalDays()) {
            throw planError();
        }
        for (int i = 0; i < points.size(); i++) {
            if (!points.get(i).pointDate().equals(start.plusDays(i))) {
                throw planError();
            }
        }
    }

    private static BusinessException planError() {
        return new BusinessException(ErrorCode.INTERNAL_ERROR, "학습 계획을 만드는 중 오류가 발생했습니다. 다시 시도해 주세요.");
    }
}
