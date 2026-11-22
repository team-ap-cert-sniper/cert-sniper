package com.certsniper.admin.entity;

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

/** 관리자 변경 이력 (admin_audit_logs) */
@Entity
@Table(name = "admin_audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAuditLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "action", nullable = false, length = 40)
    private String action;

    @Column(name = "target_table", nullable = false, length = 64)
    private String targetTable;

    @Column(name = "target_id", nullable = false, length = 50)
    private String targetId;

    @Column(name = "before_json", columnDefinition = "json")
    private String beforeJson;

    @Column(name = "after_json", columnDefinition = "json")
    private String afterJson;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "request_id", columnDefinition = "char(36)")
    private String requestId;

    @Column(name = "client_ip", length = 45)
    private String clientIp;
}
