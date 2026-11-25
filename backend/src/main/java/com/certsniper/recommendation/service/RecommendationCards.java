package com.certsniper.recommendation.service;

import com.certsniper.examschedule.dto.ExamScheduleResponse;
import com.certsniper.examschedule.entity.QualificationExamSchedule;
import com.certsniper.examschedule.repository.QualificationExamScheduleRepository;
import com.certsniper.favorite.entity.Favorite;
import com.certsniper.favorite.repository.FavoriteRepository;
import com.certsniper.job.entity.InterestField;
import com.certsniper.job.entity.JobCategory;
import com.certsniper.job.repository.InterestFieldRepository;
import com.certsniper.job.repository.JobCategoryRepository;
import com.certsniper.qualification.dto.PassRateResponse;
import com.certsniper.qualification.entity.Qualification;
import com.certsniper.qualification.entity.QualificationStatisticsYearly;
import com.certsniper.qualification.repository.QualificationStatisticsYearlyRepository;
import com.certsniper.qualification.service.QualificationCatalog;
import com.certsniper.recommendation.dto.CardInfo;
import com.certsniper.recommendation.dto.CardInfo.PassRateLatest;
import com.certsniper.recommendation.dto.ScoreBreakdown;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 추천 카드 표시값(한 줄 소개·최근 합격률·다음 접수·가까운 시험)과 시험일정·찜 조회. 전공 맞춤·조건 추천 공용 */
@Component
@RequiredArgsConstructor
class RecommendationCards {

    /** 같은 해 안에서 단계 표시 순서 (필기→실기, 1차→2차→3차→면접) */
    private static final Map<String, Integer> STAGE_ORDER =
            Map.of("필기", 0, "1차", 1, "2차", 2, "3차", 3, "실기", 4, "면접", 5, "시험", 6);

    private final QualificationExamScheduleRepository scheduleRepository;
    private final QualificationStatisticsYearlyRepository statisticsRepository;
    private final InterestFieldRepository interestFieldRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final FavoriteRepository favoriteRepository;

    /** 종목코드 → 시험일정 (시험일 순) */
    Map<String, List<QualificationExamSchedule>> schedules(Collection<String> jmcds) {
        return jmcds.isEmpty() ? Map.of()
                : scheduleRepository.findByJmcdInOrderByExamStartDateAscIdAsc(jmcds).stream()
                        .collect(Collectors.groupingBy(QualificationExamSchedule::getJmcd));
    }

    /** 기준일 이후 남은 시험 점수 (없으면 null) */
    static ScoreBreakdown upcoming(List<QualificationExamSchedule> schedules, LocalDate today) {
        return schedules.stream()
                .filter(s -> !s.getExamStartDate().isBefore(today))
                .findFirst()
                .map(s -> new ScoreBreakdown("UPCOMING_2026", 10, "남은 시험 " + s.getExamStartDate() + " " + s.getExamStage()))
                .orElse(null);
    }

    /** 찜한 종목코드 */
    Set<String> favorites(Long userId) {
        return userId == null ? Set.of()
                : favoriteRepository.findByUserId(userId).stream().map(Favorite::getJmcd).collect(Collectors.toSet());
    }

    /** 종목코드 → 카드 표시값 */
    Map<String, CardInfo> cards(List<Qualification> qualifications,
            Map<String, List<QualificationExamSchedule>> schedules, LocalDate today) {
        if (qualifications.isEmpty()) {
            return Map.of();
        }
        Map<String, List<QualificationStatisticsYearly>> stats = statisticsRepository
                .findByJmcdInOrderByIdAsc(qualifications.stream().map(Qualification::getJmcd).toList()).stream()
                .collect(Collectors.groupingBy(QualificationStatisticsYearly::getJmcd));
        Map<String, String> fields = interestFieldRepository.findAll().stream()
                .collect(Collectors.toMap(InterestField::getCode, InterestField::getName));
        Map<String, String> jobs = jobCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(JobCategory::getCode, JobCategory::getName));
        return qualifications.stream().collect(Collectors.toMap(Qualification::getJmcd, q -> {
            List<ExamScheduleResponse> active = schedules.getOrDefault(q.getJmcd(), List.of()).stream()
                    .map(s -> ExamScheduleResponse.of(s, today))
                    .filter(s -> !ExamScheduleResponse.ENDED.equals(s.examStatus()))
                    .toList();
            ExamScheduleResponse nextRegistration = active.stream()
                    .filter(s -> ExamScheduleResponse.UPCOMING.equals(s.examStatus())
                            && !ExamScheduleResponse.CLOSED.equals(s.registrationStatus()))
                    .findFirst().orElse(null);
            return new CardInfo(q.getQualgbcd(), QualificationCatalog.qualgbnm(q.getQualgbcd()), q.getGradeLevel(),
                    jobs.get(q.getMdobligfldcd()), fields.get(q.getObligfldcd()), q.getSummary(),
                    passRateLatest(stats.getOrDefault(q.getJmcd(), List.of())),
                    nextRegistration, active.isEmpty() ? null : active.get(0));
        }));
    }

    /** 최근 연도 단계별 합격률 */
    private static PassRateLatest passRateLatest(List<QualificationStatisticsYearly> rows) {
        Short year = rows.stream().map(QualificationStatisticsYearly::getYear).max(Comparator.naturalOrder()).orElse(null);
        List<PassRateResponse> items = rows.stream()
                .filter(r -> r.getYear().equals(year))
                .sorted(Comparator.comparingInt(r -> STAGE_ORDER.getOrDefault(r.getExamStage(), 99)))
                .map(r -> new PassRateResponse(null, r.getExamStage(), null, r.getExaminees(), r.getPassers(),
                        null, r.getPassRatePct(), null, null))
                .toList();
        return items.isEmpty() ? null : new PassRateLatest(year, items);
    }
}
