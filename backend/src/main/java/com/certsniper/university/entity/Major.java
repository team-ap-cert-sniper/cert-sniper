package com.certsniper.university.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 학과·전공 (majors) */
@Entity
@Table(name = "majors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Major {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "university_id", nullable = false)
    private Long universityId;

    @Column(name = "college_id")
    private Long collegeId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "parent_name", length = 100)
    private String parentName;

    @Column(name = "aggregate_college_id")
    private Long aggregateCollegeId;
}
