package com.certsniper.qualification.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import com.certsniper.job.entity.InterestField;
import com.certsniper.job.repository.InterestFieldRepository;
import com.certsniper.qualification.dto.TrendResponse;
import com.certsniper.qualification.dto.TrendResponse.TrendItem;
import com.certsniper.qualification.dto.TrendResponse.YearExaminees;
import com.certsniper.qualification.entity.Qualification;
import com.certsniper.qualification.entity.QualificationStatisticsYearly;
import com.certsniper.qualification.repository.QualificationStatisticsYearlyRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주목받는 자격증 트렌드 (etl service.trends와 같은 규칙).
 * 최근 완결 연도 1차(필기 > 1차 > 단일시험) 실제 응시자 많은 순, 국가기술·국가전문 따로 순위.
 * 기준연도 포함 5개 연도 첫해 대비 증감률이 ±10%를 넘으면 UP/DOWN, 안이면 STEADY, 첫해 자료가 없거나 0명이면 NEW_OR_SHORT_HISTORY.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrendService {

    /** 1차 시험 단계 (앞에 있을수록 우선) */
    private static final List<String> FIRST_STAGES = List.of("필기", "1차", "시험");
    private static final Set<String> GRADE_LEVELS = Set.of("기능사", "산업기사", "기사", "기능장", "기술사", "서비스");
    private static final int TREND_YEARS = 5;
    private static final BigDecimal THRESHOLD = BigDecimal.TEN;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final QualificationStatisticsYearlyRepository statisticsRepository;
    private final QualificationCatalog qualificationCatalog;
    private final InterestFieldRepository interestFieldRepository;

    public TrendResponse getTrends(String qualgbcd, String gradeLevel, String interestFieldCode, Short year, int limit) {
        String grade = blankToNull(gradeLevel);
        String field = blankToNull(interestFieldCode);
        validate(qualgbcd, grade, field, limit);
        Short baseYear = year != null ? year : latestYear();
        if (baseYear == null) {
            return new TrendResponse(null, qualgbcd, grade, field, null, 0, List.of());
        }
        String basis = baseYear + "년 1차(필기·1차·단일시험) 실제 응시자 수";

        short firstYear = (short) (baseYear - TREND_YEARS + 1);
        Map<String, Map<Short, QualificationStatisticsYearly>> stats = firstStages(
                statisticsRepository.findByYearBetweenAndExamStageInOrderByIdAsc(firstYear, baseYear, FIRST_STAGES));
        List<Qualification> ranked = qualificationCatalog.all().values().stream()
                .filter(q -> qualgbcd.equals(q.getQualgbcd())
                        && (grade == null || grade.equals(q.getGradeLevel()))
                        && (field == null || field.equals(q.getObligfldcd()))
                        && stats.getOrDefault(q.getJmcd(), Map.of()).containsKey(baseYear))
                .sorted(Comparator.comparingInt((Qualification q) -> -examinees(stats.get(q.getJmcd()).get(baseYear)))
                        .thenComparing(Qualification::getJmcd))
                .toList();

        Map<String, String> fieldNames = interestFieldRepository.findAll().stream()
                .collect(Collectors.toMap(InterestField::getCode, InterestField::getName));
        List<TrendItem> items = IntStream.range(0, Math.min(limit, ranked.size()))
                .mapToObj(i -> item(i + 1, ranked.get(i), stats.get(ranked.get(i).getJmcd()), baseYear, fieldNames))
                .toList();
        return new TrendResponse(baseYear, qualgbcd, grade, field, basis, ranked.size(), items);
    }

    /** 최근 완결 연도: 올해 이전 중 통계가 있는 마지막 해 (올해는 집계 중) */
    public Short latestYear() {
        return statisticsRepository.findFirstByYearLessThanOrderByYearDesc((short) DateUtils.today().getYear())
                .map(QualificationStatisticsYearly::getYear)
                .orElse(null);
    }

    /** 종목코드 → 그 해 1차 실제 응시자 수 */
    public Map<String, Integer> firstStageExaminees(Collection<String> jmcds, Short year) {
        if (jmcds.isEmpty() || year == null) {
            return Map.of();
        }
        return statisticsRepository.findByJmcdInAndYearAndExamStageInOrderByIdAsc(jmcds, year, FIRST_STAGES).stream()
                .collect(Collectors.toMap(QualificationStatisticsYearly::getJmcd, r -> r, TrendService::firstStage))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> examinees(e.getValue())));
    }

    private void validate(String qualgbcd, String grade, String field, int limit) {
        if (!"T".equals(qualgbcd) && !"S".equals(qualgbcd)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "qualgbcd는 T 또는 S만 가능합니다.");
        }
        if (grade != null && (!GRADE_LEVELS.contains(grade) || "S".equals(qualgbcd))) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "등급은 국가기술자격에서 기능사·산업기사·기사·기능장·기술사·서비스 중 하나입니다.");
        }
        if (field != null && !interestFieldRepository.existsById(field)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "관심 분야를 다시 확인해 주세요.");
        }
        if (limit < 1 || limit > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "limit은 1~100이어야 합니다.");
        }
    }

    private static TrendItem item(int rank, Qualification q, Map<Short, QualificationStatisticsYearly> byYear,
            short year, Map<String, String> fieldNames) {
        QualificationStatisticsYearly current = byYear.get(year);
        short firstYear = (short) (year - TREND_YEARS + 1);
        List<YearExaminees> series = IntStream.rangeClosed(firstYear, year)
                .mapToObj(y -> (short) y)
                .filter(byYear::containsKey)
                .map(y -> new YearExaminees(y, examinees(byYear.get(y))))
                .toList();
        QualificationStatisticsYearly first = byYear.get(firstYear);
        BigDecimal change = first == null ? null
                : percent(examinees(current) - examinees(first), examinees(first), 1);
        String trend = change == null ? "NEW_OR_SHORT_HISTORY"
                : change.compareTo(THRESHOLD) > 0 ? "UP"
                : change.compareTo(THRESHOLD.negate()) < 0 ? "DOWN" : "STEADY";
        int passers = current.getPassers() == null ? 0 : current.getPassers();
        return new TrendItem(rank, q.getJmcd(), q.getJmfldnm(), q.getQualgbcd(), q.getGradeLevel(), q.getObligfldcd(),
                fieldNames.get(q.getObligfldcd()), year, current.getExamStage(), examinees(current), passers,
                percent(passers, examinees(current), 2), series, change, trend);
    }

    /** 종목코드 → 연도 → 그 해 첫 단계 통계 */
    private static Map<String, Map<Short, QualificationStatisticsYearly>> firstStages(
            List<QualificationStatisticsYearly> rows) {
        Map<String, Map<Short, QualificationStatisticsYearly>> result = new HashMap<>();
        rows.forEach(r -> result.computeIfAbsent(r.getJmcd(), k -> new HashMap<>())
                .merge(r.getYear(), r, TrendService::firstStage));
        return result;
    }

    /** 같은 해 두 단계 중 앞 단계 (같으면 먼저 읽은 행) */
    private static QualificationStatisticsYearly firstStage(QualificationStatisticsYearly a, QualificationStatisticsYearly b) {
        return FIRST_STAGES.indexOf(b.getExamStage()) < FIRST_STAGES.indexOf(a.getExamStage()) ? b : a;
    }

    private static int examinees(QualificationStatisticsYearly row) {
        return row.getExaminees() == null ? 0 : row.getExaminees();
    }

    /** part ÷ whole × 100 사사오입 (etl percent와 같음). whole이 0이면 null */
    private static BigDecimal percent(int part, int whole, int places) {
        return whole == 0 ? null
                : BigDecimal.valueOf(part).multiply(HUNDRED).divide(BigDecimal.valueOf(whole), places, RoundingMode.HALF_UP);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
