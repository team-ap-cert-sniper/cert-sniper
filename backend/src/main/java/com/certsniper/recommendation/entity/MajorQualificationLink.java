package com.certsniper.recommendation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 학과–자격증 연결 (major_qualification_links) */
@Entity
@Table(name = "major_qualification_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MajorQualificationLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "major_id", nullable = false)
    private Long majorId;

    @Column(name = "jmcd", nullable = false, length = 20)
    private String jmcd;

    @Column(name = "link_rank", nullable = false)
    private Short linkRank;

    @Column(name = "basis", nullable = false, length = 30)
    private String basis;
}
