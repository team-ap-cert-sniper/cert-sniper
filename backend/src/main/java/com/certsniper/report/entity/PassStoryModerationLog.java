package com.certsniper.report.entity;

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

/** 게시글 상태 변경 이력 (pass_story_moderation_logs) */
@Entity
@Table(name = "pass_story_moderation_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PassStoryModerationLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "story_id", nullable = false)
    private Long storyId;

    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "actor_role", nullable = false, length = 10)
    private String actorRole;

    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "reason_code", length = 20)
    private String reasonCode;

    @Column(name = "reason_text", length = 500)
    private String reasonText;

    @Column(name = "previous_status", length = 20)
    private String previousStatus;

    @Column(name = "new_status", nullable = false, length = 20)
    private String newStatus;

    @Column(name = "previous_content", columnDefinition = "json")
    private String previousContent;

    public static PassStoryModerationLog of(Long storyId, String action, Long actorId, String actorRole,
            Long reportId, String reasonCode, String previousStatus, String newStatus) {
        PassStoryModerationLog l = new PassStoryModerationLog();
        l.storyId = storyId;
        l.action = action;
        l.actorId = actorId;
        l.actorRole = actorRole;
        l.reportId = reportId;
        l.reasonCode = reasonCode;
        l.previousStatus = previousStatus;
        l.newStatus = newStatus;
        return l;
    }
}
