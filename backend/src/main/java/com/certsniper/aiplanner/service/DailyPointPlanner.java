package com.certsniper.aiplanner.service;

import com.certsniper.aiplanner.dto.AiDraft;
import com.certsniper.aiplanner.dto.AiPlanResponse;
import com.certsniper.aiplanner.dto.SurveyRequest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * 하이브리드 방식의 서버 담당 부분.
 * AI 초안(일정바 제목·대략 일수·주제)을 받아서
 * ① 시작일 ~ 시험 전날 기간을 일수 비율대로 나눠 일정바 날짜를 정하고
 * ② 날마다 가능 요일이면 주제를 순서대로, 아니면 "휴식"을 넣는다.
 */
@Component
public class DailyPointPlanner {

    static final String REST = "휴식";
    static final String REVIEW = " 복습";

    public AiPlanResponse expand(String jmcd, SurveyRequest survey, AiDraft draft) {
        LocalDate start = survey.startDate();
        LocalDate lastDay = survey.examDate().minusDays(1);
        int totalDays = (int) ChronoUnit.DAYS.between(start, lastDay) + 1;
        Set<DayOfWeek> studyDays = Set.copyOf(survey.studyDays());

        // 일정바가 기간(일수)보다 많으면 앞에서부터 기간만큼만 쓴다 (하루에 일정바 1개 이상)
        List<AiDraft.Bar> bars = draft.bars().subList(0, Math.min(draft.bars().size(), totalDays));
        int[] lengths = splitDays(bars, totalDays);

        List<AiPlanResponse.TimelineBar> timelineBars = new ArrayList<>();
        List<AiPlanResponse.DailyPoint> dailyPoints = new ArrayList<>();
        LocalDate barStart = start;
        for (int i = 0; i < bars.size(); i++) {
            AiDraft.Bar bar = bars.get(i);
            LocalDate barEnd = barStart.plusDays(lengths[i] - 1L);
            timelineBars.add(new AiPlanResponse.TimelineBar(bar.title(), barStart, barEnd));

            List<String> topics = bar.topics() == null || bar.topics().isEmpty() ? List.of(bar.title()) : bar.topics();
            int studyCount = 0;
            for (LocalDate day = barStart; !day.isAfter(barEnd); day = day.plusDays(1)) {
                if (!studyDays.contains(day.getDayOfWeek())) {
                    dailyPoints.add(new AiPlanResponse.DailyPoint(day, REST, true));
                    continue;
                }
                String topic = topics.get(studyCount % topics.size());
                // 주제를 한 바퀴 다 돌았으면 두 번째부터는 복습
                String content = studyCount < topics.size() ? topic : topic + REVIEW;
                dailyPoints.add(new AiPlanResponse.DailyPoint(day, content, false));
                studyCount++;
            }
            barStart = barEnd.plusDays(1);
        }
        return new AiPlanResponse(new AiPlanResponse.Meta(jmcd, start, survey.examDate(), totalDays),
                timelineBars, dailyPoints);
    }

    /**
     * 전체 일수를 일정바마다 AI가 준 일수 비율대로 나눈다. 일정바마다 최소 1일, 합계는 정확히 totalDays.
     * (최소 1일씩 먼저 주고, 남은 일수를 비율대로 나눈 뒤 소수점 때문에 남는 날은 소수점이 큰 순서로 하루씩 더 준다)
     */
    static int[] splitDays(List<AiDraft.Bar> bars, int totalDays) {
        int n = bars.size();
        int[] lengths = new int[n];
        long weightSum = 0;
        for (AiDraft.Bar bar : bars) {
            weightSum += Math.max(1, bar.days());
        }
        int remaining = totalDays - n;
        double[] fractions = new double[n];
        int given = 0;
        for (int i = 0; i < n; i++) {
            double share = (double) remaining * Math.max(1, bars.get(i).days()) / weightSum;
            int floor = (int) Math.floor(share);
            lengths[i] = 1 + floor;
            fractions[i] = share - floor;
            given += floor;
        }
        for (int left = remaining - given; left > 0; left--) {
            int best = 0;
            for (int i = 1; i < n; i++) {
                if (fractions[i] > fractions[best]) {
                    best = i;
                }
            }
            lengths[best]++;
            fractions[best] = -1;
        }
        return lengths;
    }
}
