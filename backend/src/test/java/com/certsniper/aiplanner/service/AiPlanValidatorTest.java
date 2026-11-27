package com.certsniper.aiplanner.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.certsniper.aiplanner.dto.AiDraft;
import com.certsniper.aiplanner.dto.AiPlanResponse;
import com.certsniper.aiplanner.dto.SurveyRequest;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiPlanValidatorTest {

    private final AiPlanValidator validator = new AiPlanValidator();
    private final DailyPointPlanner planner = new DailyPointPlanner();

    /** 날짜가 지나도 테스트가 깨지지 않게 오늘 기준으로 잡는다 */
    private static final LocalDate TODAY = DateUtils.today();
    private static final List<DayOfWeek> EVERY_DAY = Arrays.asList(DayOfWeek.values());

    private static SurveyRequest survey(LocalDate start, LocalDate exam, List<DayOfWeek> studyDays) {
        return new SurveyRequest("2290", 7L, exam, start, "처음 공부", 2, studyDays, 60);
    }

    private static ErrorCode surveyError(AiPlanValidator validator, SurveyRequest survey) {
        return assertThrows(BusinessException.class, () -> validator.validateSurvey(survey)).getErrorCode();
    }

    // ===== ① 설문 날짜 =====

    @Test
    void 정상_설문은_통과() {
        assertDoesNotThrow(() -> validator.validateSurvey(survey(TODAY, TODAY.plusDays(30), EVERY_DAY)));
    }

    @Test
    void 시작일이_지난_날이면_400() {
        assertEquals(ErrorCode.VALIDATION_ERROR,
                surveyError(validator, survey(TODAY.minusDays(1), TODAY.plusDays(30), EVERY_DAY)));
    }

    @Test
    void 시작일이_시험일과_같거나_뒤면_400() {
        assertEquals(ErrorCode.VALIDATION_ERROR,
                surveyError(validator, survey(TODAY.plusDays(10), TODAY.plusDays(10), EVERY_DAY)));
    }

    @Test
    void 기간이_1년을_넘으면_400() {
        assertEquals(ErrorCode.VALIDATION_ERROR,
                surveyError(validator, survey(TODAY, TODAY.plusDays(400), EVERY_DAY)));
    }

    @Test
    void 기간_안에_공부_가능_요일이_없으면_400() {
        // 시작일 하루만 공부 기간인데, 그 요일이 공부 가능 요일이 아님
        DayOfWeek other = TODAY.getDayOfWeek().plus(1);
        assertEquals(ErrorCode.VALIDATION_ERROR,
                surveyError(validator, survey(TODAY, TODAY.plusDays(1), List.of(other))));
    }

    // ===== ② 초안 다듬기 =====

    @Test
    void 빈_제목은_채우고_빈_주제는_뺀다() {
        AiDraft draft = new AiDraft(List.of(new AiDraft.Bar("  ", 5, Arrays.asList(" 엔터티 ", "", null, "속성"))));

        AiDraft.Bar bar = validator.sanitize(draft).bars().get(0);

        assertEquals("1단계 학습", bar.title());
        assertEquals(List.of("엔터티", "속성"), bar.topics());
    }

    @Test
    void 일정바는_최대_8개() {
        List<AiDraft.Bar> many = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            many.add(new AiDraft.Bar("단계" + i, 3, List.of("주제")));
        }
        assertEquals(8, validator.sanitize(new AiDraft(many)).bars().size());
    }

    // ===== ③ 최종 플랜 =====

    @Test
    void 서버가_만든_플랜은_통과() {
        SurveyRequest survey = survey(TODAY, TODAY.plusDays(20), EVERY_DAY);
        AiDraft draft = new AiDraft(List.of(new AiDraft.Bar("이론", 2, List.of("a")), new AiDraft.Bar("기출", 1, List.of())));

        assertDoesNotThrow(() -> validator.validatePlan(planner.expand("2290", survey, draft)));
    }

    @Test
    void 일정바_사이에_빈틈이_있으면_500() {
        AiPlanResponse broken = new AiPlanResponse(
                new AiPlanResponse.Meta("2290", TODAY, TODAY.plusDays(5), 5),
                List.of(new AiPlanResponse.TimelineBar("a", TODAY, TODAY.plusDays(1)),
                        new AiPlanResponse.TimelineBar("b", TODAY.plusDays(3), TODAY.plusDays(4))),
                List.of());

        BusinessException e = assertThrows(BusinessException.class, () -> validator.validatePlan(broken));
        assertEquals(ErrorCode.INTERNAL_ERROR, e.getErrorCode());
    }
}
