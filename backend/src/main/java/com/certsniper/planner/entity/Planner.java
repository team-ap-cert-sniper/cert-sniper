package com.certsniper.planner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 플래너 다이어리 (planners) */
@Entity
@Table(name = "planners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Planner {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "mode", nullable = false, length = 20)
    private String mode = "MANUAL";
}
