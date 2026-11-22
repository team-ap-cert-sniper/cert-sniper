package com.certsniper.planner.entity;

import com.certsniper.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 개인 일정 (personal_schedules) */
@Entity
@Table(name = "personal_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "display_color", nullable = false, columnDefinition = "char(7)")
    private String displayColor;
}
