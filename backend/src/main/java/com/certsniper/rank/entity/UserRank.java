package com.certsniper.rank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 회원 저격 등급 (user_ranks) */
@Entity
@Table(name = "user_ranks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRank {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "valid_cert_count", nullable = false)
    private Integer validCertCount = 0;

    @Column(name = "current_rank", nullable = false, length = 20)
    private String currentRank = "TRAINEE";
}
