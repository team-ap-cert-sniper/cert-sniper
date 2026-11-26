package com.certsniper.planner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 세부 일정·마일스톤 (planner_milestones) */
@Entity
@Table(name = "planner_milestones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlannerMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "bar_id", nullable = false)
    private Long barId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "point_date", nullable = false)
    private LocalDate pointDate;

    @Column(name = "content", nullable = false, length = 100)
    private String content;

    @Column(name = "is_rest", nullable = false)
    private Boolean isRest = false;

    @Column(name = "source", nullable = false, length = 10)
    private String source = "MANUAL";

    /** 세부 일정 생성 */
    public static PlannerMilestone create(Long barId, Long userId, LocalDate pointDate, String content,
                                          boolean isRest, String source) {
        PlannerMilestone milestone = new PlannerMilestone();
        milestone.barId = barId;
        milestone.userId = userId;
        milestone.pointDate = pointDate;
        milestone.content = content;
        milestone.isRest = isRest;
        milestone.source = source;
        return milestone;
    }
}
