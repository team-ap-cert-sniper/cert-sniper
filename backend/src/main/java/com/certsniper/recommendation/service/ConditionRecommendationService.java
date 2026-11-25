package com.certsniper.recommendation.service;

import com.certsniper.examschedule.entity.QualificationExamSchedule;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import com.certsniper.job.repository.InterestFieldRepository;
import com.certsniper.job.repository.JobCategoryRepository;
import com.certsniper.qualification.entity.Qualification;
import com.certsniper.qualification.entity.QualificationStatisticsYearly;
import com.certsniper.qualification.repository.QualificationStatisticsYearlyRepository;
import com.certsniper.qualification.service.QualificationCatalog;
import com.certsniper.recommendation.dto.CardInfo;
import com.certsniper.recommendation.dto.ConditionRecommendationRequest;
import com.certsniper.recommendation.dto.RecommendationItem;
import com.certsniper.recommendation.dto.RecommendationItem.InterestMatch;
import com.certsniper.recommendation.dto.RecommendationItem.RecentExaminees;
import com.certsniper.recommendation.dto.RecommendationResponse;
import com.certsniper.recommendation.dto.ScoreBreakdown;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 조건 선택 추천 (etl service.recommend_by_condition과 같은 규칙). 대학교·전공·학년은 쓰지 않는다.
 * 점수 = 희망직무 일치 +30, 관심분야 일치 +20, 올해 남은 시험 +10, 응시 조건 없음 +5.
 * 동점이면 최근 완결 연도 1차 응시자 많은 순 → 종목코드 순. 보유 자격과 그 하위 등급은 제외.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConditionRecommendationService {

    /** 1차 시험 단계 (앞에 있을수록 우선: 필기 > 1차 > 시험) */
    private static final List<String> FIRST_STAGES = List.of("필기", "1차", "시험");

    private final QualificationCatalog qualificationCatalog;
    private final InterestFieldRepository interestFieldRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final QualificationStatisticsYearlyRepository statisticsRepository;
    private final RecommendationCards recommendationCards;

    @Value("${app.schema-version}")
    private String schemaVersion;

    private record Scored(Qualification qualification, int score, List<ScoreBreakdown> breakdown,
            List<InterestMatch> matches, int examinees) {}

    public RecommendationResponse recommendByCondition(Long userId, ConditionRecommendationRequest request) {
        LocalDate today = DateUtils.today();
        Set<String> jobs = codes(request.jobCategoryCodes(), jobCategoryRepository);
        Set<String> fields = codes(request.interestFieldCodes(), interestFieldRepository);
        if (jobs.isEmpty() && fields.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "관심 분야 또는 희망 직무를 하나 이상 선택해 주세요.");
        }
        Map<String, Qualification> catalog = qualificationCatalog.all();
        Set<String> excluded = ExamWindowRules.excluded(qualificationCatalog.owned(request.ownedJmcds()), catalog.values());
        List<Qualification> matched = catalog.values().stream()
                .filter(q -> !excluded.contains(q.getJmcd())
                        && (request.qualgbcd() == null || request.qualgbcd().equals(q.getQualgbcd()))
                        && (jobs.contains(q.getMdobligfldcd()) || fields.contains(q.getObligfldcd())))
                .toList();

        List<String> codes = matched.stream().map(Qualification::getJmcd).toList();
        Map<String, List<QualificationExamSchedule>> schedules = recommendationCards.schedules(codes);
        Short year = statisticsRepository.findFirstByYearLessThanOrderByYearDesc((short) today.getYear())
                .map(QualificationStatisticsYearly::getYear).orElse(null);
        Map<String, Integer> examinees = firstStageExaminees(codes, year);

        List<Scored> top = matched.stream()
                .map(q -> score(q, jobs, fields, schedules.getOrDefault(q.getJmcd(), List.of()),
                        examinees.getOrDefault(q.getJmcd(), 0), today))
                .sorted(Comparator.comparingInt((Scored s) -> -s.score())
                        .thenComparingInt(s -> -s.examinees())
                        .thenComparing(s -> s.qualification().getJmcd()))
                .limit(RecommendationService.RESULT_LIMIT)
                .toList();

        Map<String, CardInfo> cards = recommendationCards.cards(
                top.stream().map(Scored::qualification).toList(), schedules, today);
        Set<String> favorites = recommendationCards.favorites(userId);
        Integer recentYear = year == null ? null : year.intValue();
        List<RecommendationItem> items = top.stream()
                .map(s -> {
                    Qualification q = s.qualification();
                    String eligibilityRequired = q.getEligibilityRequired();
                    return new RecommendationItem(q.getJmcd(), q.getJmfldnm(), null, null, null, s.matches(),
                            s.score(), s.breakdown(), new RecentExaminees(recentYear, s.examinees()),
                            eligibilityRequired == null || eligibilityRequired.isBlank() ? null : eligibilityRequired,
                            cards.get(q.getJmcd()), favorites.contains(q.getJmcd()));
                })
                .toList();
        return new RecommendationResponse(schemaVersion, "CONDITION", null, null, items, matched.size(),
                RecommendationService.RESULT_LIMIT, null, today);
    }

    /** 선택 코드 검증: 중복 없음, 목록에 있는 코드 */
    private static Set<String> codes(List<String> values, JpaRepository<?, String> repository) {
        List<String> codes = values == null ? List.of() : values;
        Set<String> unique = new HashSet<>(codes);
        if (unique.size() != codes.size() || unique.contains(null) || repository.findAllById(unique).size() != unique.size()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "관심 분야·희망 직무를 다시 확인해 주세요.");
        }
        return unique;
    }

    /** 종목코드 → 그 연도 1차 응시자 수 (필기 > 1차 > 시험 순으로 그 종목의 첫 단계) */
    private Map<String, Integer> firstStageExaminees(List<String> codes, Short year) {
        if (codes.isEmpty() || year == null) {
            return Map.of();
        }
        return statisticsRepository.findByJmcdInAndYearAndExamStageInOrderByIdAsc(codes, year, FIRST_STAGES).stream()
                .collect(Collectors.toMap(QualificationStatisticsYearly::getJmcd, r -> r,
                        (a, b) -> FIRST_STAGES.indexOf(b.getExamStage()) < FIRST_STAGES.indexOf(a.getExamStage()) ? b : a))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().getExaminees() == null ? 0 : e.getValue().getExaminees()));
    }

    private static Scored score(Qualification q, Set<String> jobs, Set<String> fields,
            List<QualificationExamSchedule> schedules, int examinees, LocalDate today) {
        List<ScoreBreakdown> parts = new ArrayList<>();
        List<InterestMatch> matches = new ArrayList<>();
        if (jobs.contains(q.getMdobligfldcd())) {
            parts.add(new ScoreBreakdown("JOB_CATEGORY", 30, "희망직무 일치"));
            matches.add(new InterestMatch("JOB_CATEGORY", q.getMdobligfldcd()));
        }
        if (fields.contains(q.getObligfldcd())) {
            parts.add(new ScoreBreakdown("INTEREST_FIELD", 20, "관심분야 일치"));
            matches.add(new InterestMatch("INTEREST_FIELD", q.getObligfldcd()));
        }
        ScoreBreakdown upcoming = RecommendationCards.upcoming(schedules, today);
        if (upcoming != null) {
            parts.add(upcoming);
        }
        if ("N".equals(q.getEligibilityRequired())) {
            parts.add(new ScoreBreakdown("NO_ELIGIBILITY_REQUIRED", 5, "응시 조건 없음(바로 응시 가능)"));
        }
        return new Scored(q, parts.stream().mapToInt(ScoreBreakdown::points).sum(), parts, matches, examinees);
    }
}
