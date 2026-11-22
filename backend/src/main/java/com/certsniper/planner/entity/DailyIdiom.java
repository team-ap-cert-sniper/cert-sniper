package com.certsniper.planner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 오늘의 사자성어 (daily_idioms) */
@Entity
@Table(name = "daily_idioms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyIdiom {

    @Column(name = "idiom", nullable = false, length = 10)
    private String idiom;

    @Column(name = "hanja", length = 10)
    private String hanja;

    @Column(name = "meaning", nullable = false, length = 200)
    private String meaning;

    @Id
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}
