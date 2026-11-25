package com.certsniper.recommendation.service;

import static com.certsniper.recommendation.service.ExamWindowRules.AFTER_GRADUATION_EXPERIENCE;
import static com.certsniper.recommendation.service.ExamWindowRules.CHECK_QUALIFICATION;
import static com.certsniper.recommendation.service.ExamWindowRules.FROM_GRADE;
import static com.certsniper.recommendation.service.ExamWindowRules.NOW;
import static com.certsniper.recommendation.service.ExamWindowRules.NO_RESTRICTION;
import static com.certsniper.recommendation.service.ExamWindowRules.isDigit;
import static com.certsniper.recommendation.service.ExamWindowRules.isTechnical;

import com.certsniper.examschedule.entity.QualificationExamSchedule;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import com.certsniper.option.service.OptionService;
import com.certsniper.qualification.entity.Qualification;
import com.certsniper.qualification.service.QualificationCatalog;
import com.certsniper.recommendation.dto.CardInfo;
import com.certsniper.recommendation.dto.RecommendationItem;
import com.certsniper.recommendation.dto.RecommendationItem.ExamWindow;
import com.certsniper.recommendation.dto.RecommendationItem.FacultyShare;
import com.certsniper.recommendation.dto.RecommendationRequest;
import com.certsniper.recommendation.dto.RecommendationResponse;
import com.certsniper.recommendation.dto.ScoreBreakdown;
import com.certsniper.recommendation.entity.MajorQualificationLink;
import com.certsniper.recommendation.repository.MajorQualificationLinkRepository;
import com.certsniper.university.entity.Major;
import com.certsniper.university.entity.University;
import com.certsniper.university.repository.MajorRepository;
import com.certsniper.university.repository.UniversityRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 전공 맞춤 추천 (etl service.recommend와 같은 규칙).
 * 점수 = 학과 연결 + 응시 시점 + 올해 남은 시험 + 시작 등급 / 재학 중 응시 불가 등급 감점. 점수순 최대 5개.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    static final int RESULT_LIMIT = 5;
    private static final String FACULTY_AGGREGATE = "faculty_aggregate";
    private static final Map<String, Integer> LINK_SCORE = Map.of(
            "existing_direct_links", 40, "recommended", 35, "same_subfield", 30, FACULTY_AGGREGATE, 40);
    private static final Map<String, String> LINK_LABEL = Map.of(
            "existing_direct_links", "학과 직접 연결", "recommended", "전공 연관", "same_subfield", "동계열 공통",
            FACULTY_AGGREGATE, "학부 소속 학과 합산");
    private static final Map<String, Integer> WINDOW_SCORE = Map.of(
            NOW, 20, NO_RESTRICTION, 20, CHECK_QUALIFICATION, 5, AFTER_GRADUATION_EXPERIENCE, 0);
    private static final Map<String, String> WINDOW_REASON = Map.of(
            NOW, "지금 학년 응시 가능", NO_RESTRICTION, "응시자격 제한 없음",
            CHECK_QUALIFICATION, "응시자격 개별 확인", AFTER_GRADUATION_EXPERIENCE, "졸업·경력 후 응시");
    private static final Set<String> UNFIT_LEVELS = Set.of("기술사", "기능장");

    private final OptionService optionService;
    private final QualificationCatalog qualificationCatalog;
    private final UniversityRepository universityRepository;
    private final MajorRepository majorRepository;
    private final MajorQualificationLinkRepository majorQualificationLinkRepository;
    private final RecommendationCards recommendationCards;

    @Value("${app.schema-version}")
    private String schemaVersion;

    /** 학과 연결 후보: 연결 근거, 연결된 소속 학과 수, 동점 정렬용 연결 순위 */
    private record Candidate(String jmcd, String basis, int share, int rank) {}

    /** 점수 계산에 쓰는 회원 조건 */
    private record Profile(String grade, String startLevel, int facultySize, LocalDate today) {}

    private record Scored(Candidate candidate, Qualification qualification, ExamWindow window,
            int score, List<ScoreBreakdown> breakdown) {}

    public RecommendationResponse recommend(Long userId, RecommendationRequest request) {
        LocalDate today = DateUtils.today();
        Map<String, Qualification> catalog = qualificationCatalog.all();
        List<Qualification> owned = validate(request, catalog);
        if (request.universityId() == null) {
            return response("NONE", true, List.of(), 0, today);
        }

        Long facultyId = request.collegeId() != null ? request.collegeId()
                : majorRepository.findById(request.majorId()).map(Major::getAggregateCollegeId).orElse(null);
        List<Long> majorIds = facultyId == null ? List.of(request.majorId())
                : majorRepository.findByCollegeId(facultyId).stream().map(Major::getId).toList();
        List<Candidate> candidates = candidates(majorIds, facultyId != null);

        Set<String> excluded = new HashSet<>(request.ownedJmcds() == null ? List.of() : request.ownedJmcds());
        excluded.addAll(ExamWindowRules.lowerLevels(owned, catalog.values()));
        List<Candidate> kept = candidates.stream().filter(c -> !excluded.contains(c.jmcd())).toList();

        String schoolType = universityRepository.findById(request.universityId())
                .map(University::getSchoolType).orElse("");
        Profile profile = new Profile(request.grade(), ExamWindowRules.startLevel(schoolType, request.grade()),
                facultyId == null ? 0 : majorIds.size(), today);
        Map<String, List<QualificationExamSchedule>> schedules =
                recommendationCards.schedules(kept.stream().map(Candidate::jmcd).toList());

        List<Scored> top = kept.stream()
                .map(c -> {
                    Qualification q = catalog.get(c.jmcd());
                    ExamWindow window = ExamWindowRules.examWindow(q, schoolType, request.grade(), owned);
                    return score(c, q, window, schedules.getOrDefault(c.jmcd(), List.of()), profile, catalog);
                })
                .sorted(Comparator.comparingInt((Scored s) -> -s.score())
                        .thenComparingInt(s -> -s.candidate().share())
                        .thenComparingInt(s -> s.candidate().rank())
                        .thenComparing(s -> s.candidate().jmcd()))
                .limit(RESULT_LIMIT)
                .toList();

        Map<String, CardInfo> cards = recommendationCards.cards(
                top.stream().map(Scored::qualification).toList(), schedules, today);
        Set<String> favorites = recommendationCards.favorites(userId);
        List<RecommendationItem> items = top.stream()
                .map(s -> new RecommendationItem(s.candidate().jmcd(), s.qualification().getJmfldnm(),
                        s.candidate().basis(),
                        profile.facultySize() > 0 ? new FacultyShare(s.candidate().share(), profile.facultySize()) : null,
                        s.window(), null, s.score(), s.breakdown(), cards.get(s.candidate().jmcd()),
                        favorites.contains(s.candidate().jmcd())))
                .toList();
        return response(facultyId == null ? "MAJOR" : "FACULTY", false, items, candidates.size() - kept.size(), today);
    }

    /** 학교 정보 묶음·보유 자격 검증. 보유 자격 행을 종목코드 순으로 돌려준다 */
    private List<Qualification> validate(RecommendationRequest request, Map<String, Qualification> catalog) {
        if (request.collegeId() != null && request.majorId() != null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "학과와 학부 중 하나만 선택해 주세요.");
        }
        optionService.validateSchoolInfo(request.universityId(), request.collegeId(), request.majorId(), request.grade());
        List<String> codes = request.ownedJmcds() == null ? List.of() : request.ownedJmcds();
        if (new HashSet<>(codes).size() != codes.size()
                || !codes.stream().allMatch(c -> c != null && catalog.containsKey(c))) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "보유 자격을 다시 확인해 주세요.");
        }
        return codes.stream().sorted().map(catalog::get).toList();
    }

    /**
     * 학과 연결 후보. 학과 기준은 연결 순위 순,
     * 학부 합산은 소속 학과 연결을 합쳐 겹친 학과 수와 가장 높은 연결 순위를 센다.
     */
    private List<Candidate> candidates(List<Long> majorIds, boolean faculty) {
        Map<String, Candidate> result = new LinkedHashMap<>();
        List<MajorQualificationLink> links = majorIds.isEmpty() ? List.of()
                : majorQualificationLinkRepository.findByMajorIdInOrderByLinkRankAscIdAsc(majorIds);
        for (MajorQualificationLink link : links) {
            if (faculty) {
                result.merge(link.getJmcd(), new Candidate(link.getJmcd(), FACULTY_AGGREGATE, 1, link.getLinkRank()),
                        (a, b) -> new Candidate(a.jmcd(), FACULTY_AGGREGATE, a.share() + 1, Math.min(a.rank(), b.rank())));
            } else {
                result.putIfAbsent(link.getJmcd(), new Candidate(link.getJmcd(), link.getBasis(), 1, result.size()));
            }
        }
        return new ArrayList<>(result.values());
    }

    private static Scored score(Candidate c, Qualification q, ExamWindow window,
            List<QualificationExamSchedule> schedules, Profile p, Map<String, Qualification> catalog) {
        List<ScoreBreakdown> parts = new ArrayList<>();
        if (LINK_SCORE.containsKey(c.basis())) {
            boolean faculty = p.facultySize() > 0;
            double ratio = faculty ? (double) c.share() / p.facultySize() : 1.0;
            parts.add(new ScoreBreakdown("ACADEMIC_LINK", (int) Math.rint(LINK_SCORE.get(c.basis()) * ratio),
                    LINK_LABEL.get(c.basis()) + (faculty ? " " + percent(ratio) : "")));
        }
        parts.add(windowPart(window, p.grade(), catalog));
        schedules.stream()
                .filter(s -> !s.getExamStartDate().isBefore(p.today()))
                .findFirst()
                .ifPresent(s -> parts.add(new ScoreBreakdown("UPCOMING_2026", 10,
                        "남은 시험 " + s.getExamStartDate() + " " + s.getExamStage())));
        if (isTechnical(q) && p.startLevel().equals(q.getGradeLevel())) {
            parts.add(new ScoreBreakdown("LEVEL_FIT", 5, "지금 학년에 딸 수 있는 시작 등급(" + p.startLevel() + ")"));
        }
        if (isDigit(p.grade()) && isTechnical(q) && UNFIT_LEVELS.contains(q.getGradeLevel())) {
            parts.add(new ScoreBreakdown("LEVEL_UNFIT", -20, "재학 중에는 응시 불가한 등급(졸업·경력 후)"));
        }
        int score = parts.stream().mapToInt(ScoreBreakdown::points).sum();
        return new Scored(c, q, window, score, parts.stream().filter(x -> x.points() != 0).toList());
    }

    private static ScoreBreakdown windowPart(ExamWindow w, String grade, Map<String, Qualification> catalog) {
        if (FROM_GRADE.equals(w.status())) {
            int points = w.fromGrade() - Integer.parseInt(grade) == 1 ? 10 : 5;
            return new ScoreBreakdown("EXAM_WINDOW", points, w.fromGrade() + "학년부터 응시 가능");
        }
        String reason = w.viaOwnedQualification() != null
                ? "보유 " + catalog.get(w.viaOwnedQualification()).getJmfldnm() + "로 응시 가능"
                : WINDOW_REASON.get(w.status());
        return new ScoreBreakdown("EXAM_WINDOW", WINDOW_SCORE.get(w.status()), reason);
    }

    /** 비율을 정수 %로 (etl의 '{:.0%}'와 같은 반올림) */
    private static String percent(double ratio) {
        return new BigDecimal(ratio * 100).setScale(0, RoundingMode.HALF_EVEN).toPlainString() + "%";
    }

    private RecommendationResponse response(String scope, boolean needsSchoolInfo, List<RecommendationItem> items,
            int ownedExcludedCount, LocalDate today) {
        return new RecommendationResponse(schemaVersion, null, scope, needsSchoolInfo, items, RESULT_LIMIT,
                ownedExcludedCount, today);
    }
}
