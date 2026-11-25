package com.certsniper.recommendation.service;

import com.certsniper.qualification.entity.Qualification;
import com.certsniper.recommendation.dto.RecommendationItem.ExamWindow;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 학년·학제·보유 자격으로 본 응시 시점 (Q-Net 응시자격별 안내 기준, etl eligibility.py와 같은 규칙).
 * 개인 응시자격 확정이 아니다. 국가기술 등급은 qualifications.grade_level을 쓴다.
 */
final class ExamWindowRules {

    static final String NOW = "NOW";
    static final String NO_RESTRICTION = "NO_RESTRICTION";
    static final String FROM_GRADE = "FROM_GRADE";
    static final String AFTER_GRADUATION_EXPERIENCE = "AFTER_GRADUATION_EXPERIENCE";
    static final String CHECK_QUALIFICATION = "CHECK_QUALIFICATION";

    /** 국가기술 등급 (낮은 등급 → 높은 등급) */
    static final List<String> LEVELS = List.of("기능사", "산업기사", "기사", "기능장", "기술사");

    private static final Set<String> FOUR_YEAR_TYPES =
            Set.of("대학교", "산업대학", "교육대학", "사이버대학(대학)", "기술대학", "방송통신대학", "각종학교(대학)");
    private static final Set<String> JUNIOR_TYPES = Set.of("전문대학", "기능대학", "사이버대학(전문)", "각종학교(전문)");
    /** 보유 자격만으로 바로 응시 가능한 등급 (같은 등급 이상 보유) */
    private static final Set<String> OWNED_ROUTE_LEVELS = Set.of("산업기사", "기사", "기술사");
    private static final Set<String> SERVICE_OPEN = Set.of("텔레마케팅관리사", "멀티미디어콘텐츠제작전문가", "스포츠경영관리사", "이러닝운영관리사");
    private static final Pattern OPEN = Pattern.compile("제한\\s*없|응시자격\\s*:\\s*없음", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern GRADE_NUM = Pattern.compile("(\\d)급", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern GRADE_NUM_TRIM = Pattern.compile("\\s*\\d급\\s*", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern FIELD_SUFFIX = Pattern.compile("\\(.*\\)$");

    /** 국가기술자격법 시행규칙 [별표 11의2] 유사 직무분야: 중직무분야(없으면 직무분야) → 유사로 보는 분야. '*' = 모든 분야 */
    private static final Map<String, String> SIMILAR_FIELDS = Map.ofEntries(
            Map.entry("024", "14 15 16 17 18 19 20 21 22 23 24 25 26"), Map.entry("082", "141 19 232"),
            Map.entry("083", "024 20 21"), Map.entry("091", "146 24"), Map.entry("131", "22"), Map.entry("132", "22"),
            Map.entry("14", "024 082 15 16 17 18 20 24 25 26"), Map.entry("15", "024 14 164 17 18 24 25 26"),
            Map.entry("16", "024 14 17 18 20 212 213 25 26"), Map.entry("17", "024 151 16 18 20 25 262"),
            Map.entry("18", "024 151 16 17 19 25 26"), Map.entry("19", "024 082 18"),
            Map.entry("20", "024 083 14 16 17 21 25 26"), Map.entry("211", "*"), Map.entry("212", "024 165 20"),
            Map.entry("213", "024 165 20"), Map.entry("221", "024 13"), Map.entry("222", "13"), Map.entry("231", "082"),
            Map.entry("232", "082 141"), Map.entry("24", "024 14 15 25 261"),
            Map.entry("251", "024 14 15 16 17 18 19 20 21 22 23 24 26"), Map.entry("252", "024 151 16 17 18 201"),
            Map.entry("261", "024 14 15 16 17 18 202 24 25"), Map.entry("262", "024 142 16 17 201"));

    /** 별표의 직종 한정 행(261-2, 262-2, 262-3) */
    private record SpecialField(String mdobligfldcd, Pattern name, String similar) {}

    private static final List<SpecialField> SIMILAR_SPECIAL = List.of(
            new SpecialField("261", Pattern.compile("자연환경관리|자연생태복원|생물분류|토양환경"), "142 143 144 24"),
            new SpecialField("262", Pattern.compile("기상"), "024 14 16 17 18 19 20 21 22 23 24"),
            new SpecialField("262", Pattern.compile("신재생에너지발전설비"), "024 141 142 16 17 20"));

    /** 같은 종목 계열의 등급 사다리 (이름 줄기, 등급 순위) */
    record Ladder(String stem, int rank) {}

    private ExamWindowRules() {
    }

    /** 응시 시점. 보유 자격 경로가 있으면 NOW + 그 보유 종목코드 */
    static ExamWindow examWindow(Qualification q, String schoolType, String grade, List<Qualification> owned) {
        String via = ownedRoute(q, owned);
        if (via != null) {
            return new ExamWindow(NOW, null, false, via);
        }
        ExamWindow w = isTechnical(q)
                ? technicalWindow(q.getGradeLevel(), q.getJmfldnm(), schoolType, grade)
                : OPEN.matcher(head(q.getEligibility(), 120)).find()
                        ? window(NO_RESTRICTION, null, false)
                        : window(CHECK_QUALIFICATION, null, null);
        return Boolean.TRUE.equals(w.relatedMajorRequired()) && anyMajorAllowed(q)
                ? window(w.status(), w.fromGrade(), false)
                : w;
    }

    /** 학교 유형·학년 기준으로 지금 응시 가능한 가장 높은 국가기술 등급 */
    static String startLevel(String schoolType, String grade) {
        for (String level : List.of("기사", "산업기사")) {
            if (NOW.equals(technicalWindow(level, level, schoolType, grade).status())) {
                return level;
            }
        }
        return "기능사";
    }

    /** 추천에서 뺄 종목코드: 보유 자격과 그보다 낮은 같은 계열 등급 */
    static Set<String> excluded(List<Qualification> owned, Collection<Qualification> all) {
        List<Ladder> own = owned.stream().map(ExamWindowRules::ladder).filter(Objects::nonNull).toList();
        Set<String> result = owned.stream().map(Qualification::getJmcd).collect(Collectors.toCollection(HashSet::new));
        if (!own.isEmpty()) {
            all.stream()
                    .filter(q -> {
                        Ladder k = ladder(q);
                        return k != null && own.stream().anyMatch(o -> o.stem().equals(k.stem()) && k.rank() < o.rank());
                    })
                    .forEach(q -> result.add(q.getJmcd()));
        }
        return result;
    }

    static boolean isTechnical(Qualification q) {
        return "T".equals(q.getQualgbcd());
    }

    static boolean isDigit(String grade) {
        return grade != null && grade.matches("\\d+");
    }

    private static ExamWindow technicalWindow(String level, String name, String schoolType, String grade) {
        switch (level) {
            case "서비스" -> {
                if ((name.endsWith("2급") && !name.startsWith("임상심리사")) || SERVICE_OPEN.contains(name)) {
                    return window(NO_RESTRICTION, null, false);
                }
                return window(name.endsWith("1급") ? AFTER_GRADUATION_EXPERIENCE : CHECK_QUALIFICATION, null, null);
            }
            case "기능사" -> {
                return window(NO_RESTRICTION, null, false);
            }
            case "기술사", "기능장" -> {
                return window(AFTER_GRADUATION_EXPERIENCE, null, true);
            }
            default -> {
                boolean four = FOUR_YEAR_TYPES.contains(schoolType);
                boolean industrial = "산업기사".equals(level);
                if ("GRADUATED".equals(grade)) {
                    return window(four || industrial ? NOW : AFTER_GRADUATION_EXPERIENCE, null, true);
                }
                if (!(four || JUNIOR_TYPES.contains(schoolType)) || !isDigit(grade)) {
                    return window(CHECK_QUALIFICATION, null, true);
                }
                if (!industrial && !four) {
                    return window(AFTER_GRADUATION_EXPERIENCE, null, true);
                }
                // 산업기사: 4년제 과정 1/2 이수(3학년) · 전문대 최종학년(2학년), 기사: 4년제 최종학년(4학년)
                int start = industrial ? (four ? 3 : 2) : 4;
                return window(Integer.parseInt(grade) >= start ? NOW : FROM_GRADE, start, true);
            }
        }
    }

    /** 보유 자격만으로 바로 응시 가능하면 그 보유 종목코드 (동일·유사 직무분야의 같은 등급 이상) */
    private static String ownedRoute(Qualification q, List<Qualification> owned) {
        String need = q.getGradeLevel();
        if (!isTechnical(q) || !OWNED_ROUTE_LEVELS.contains(need)) {
            return null;
        }
        return owned.stream()
                .filter(o -> !o.getJmcd().equals(q.getJmcd()) && isTechnical(o) && LEVELS.contains(o.getGradeLevel())
                        && LEVELS.indexOf(o.getGradeLevel()) >= LEVELS.indexOf(need)
                        && (!"기술사".equals(need) || "기술사".equals(o.getGradeLevel()))
                        && sameOrSimilarField(q, o))
                .map(Qualification::getJmcd)
                .findFirst()
                .orElse(null);
    }

    private static boolean sameOrSimilarField(Qualification target, Qualification owned) {
        String big = target.getObligfldcd();
        String mid = target.getMdobligfldcd();
        if (isBlank(big) || isBlank(owned.getObligfldcd())) {
            return false;
        }
        if (owned.getObligfldcd().equals(big)) {
            return true;
        }
        String similar = SIMILAR_SPECIAL.stream()
                .filter(s -> s.mdobligfldcd().equals(mid) && s.name().matcher(target.getJmfldnm()).find())
                .map(SpecialField::similar)
                .findFirst()
                .orElseGet(() -> mid != null && SIMILAR_FIELDS.containsKey(mid)
                        ? SIMILAR_FIELDS.get(mid) : SIMILAR_FIELDS.getOrDefault(big, ""));
        List<String> fields = Arrays.asList(similar.split(" "));
        return fields.contains("*") || fields.contains(owned.getObligfldcd()) || fields.contains(owned.getMdobligfldcd());
    }

    /** Q-Net 시험정보 관련학과가 '모든 학과 응시가능'인 종목 */
    private static boolean anyMajorAllowed(Qualification q) {
        return q.getRelatedMajors() != null && q.getRelatedMajors().replace(" ", "").contains("모든학과");
    }

    private static Ladder ladder(Qualification q) {
        String name = q.getJmfldnm();
        Matcher m = GRADE_NUM.matcher(name);
        if (m.find()) {                                   // 1·2·3급 (서비스분야·국가전문)
            return new Ladder(GRADE_NUM_TRIM.matcher(name).replaceAll(""), -Integer.parseInt(m.group(1)));
        }
        String base = FIELD_SUFFIX.matcher(name).replaceFirst("");
        String level = q.getGradeLevel();
        if (isTechnical(q) && LEVELS.contains(level) && base.endsWith(level)) {
            // 괄호 분야까지 같은 계열
            return new Ladder(base.substring(0, base.length() - level.length()) + name.substring(base.length()),
                    LEVELS.indexOf(level));
        }
        return null;
    }

    private static ExamWindow window(String status, Integer fromGrade, Boolean relatedMajorRequired) {
        return new ExamWindow(status, fromGrade, relatedMajorRequired, null);
    }

    private static String head(String text, int length) {
        return text == null ? "" : text.substring(0, Math.min(length, text.length()));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
