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
    private String mode = MANUAL;

    public static final String MANUAL = "MANUAL";
    public static final String AI_RENDER = "AI_RENDER";

    /** 플래너 생성 (회원마다 1개, 처음 모드를 바꿀 때 만든다) */
    public static Planner create(Long userId, String mode) {
        Planner planner = new Planner();
        planner.userId = userId;
        planner.mode = mode;
        return planner;
    }

    /** 모드 전환 (AI_RENDER · MANUAL) */
    public void changeMode(String mode) {
        this.mode = mode;
    }
}
