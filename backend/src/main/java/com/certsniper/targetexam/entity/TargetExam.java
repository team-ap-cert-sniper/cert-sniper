package com.certsniper.targetexam.entity;

import com.certsniper.global.common.BaseTimeEntity;
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

/** 목표 자격증 (target_exams) */
@Entity
@Table(name = "target_exams")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TargetExam extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "jmcd", nullable = false, length = 20)
    private String jmcd;

    @Column(name = "exam_schedule_id")
    private Long examScheduleId;

    @Column(name = "exam_date")
    private LocalDate examDate;

    @Column(name = "display_color", nullable = false, columnDefinition = "char(7)")
    private String displayColor;

    /** 목표 자격증 생성 */
    public static TargetExam create(Long userId, String jmcd, Long examScheduleId, LocalDate examDate,
                                    String displayColor) {
        TargetExam targetExam = new TargetExam();
        targetExam.userId = userId;
        targetExam.jmcd = jmcd;
        targetExam.examScheduleId = examScheduleId;
        targetExam.examDate = examDate;
        targetExam.displayColor = displayColor;
        return targetExam;
    }

    /** 시험 회차·시험일 변경 */
    public void changeExam(Long examScheduleId, LocalDate examDate) {
        this.examScheduleId = examScheduleId;
        this.examDate = examDate;
    }

    /** 일정 색상 변경 */
    public void changeColor(String displayColor) {
        this.displayColor = displayColor;
    }
}
