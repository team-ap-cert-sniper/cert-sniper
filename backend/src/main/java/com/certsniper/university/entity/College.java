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

/** 학부·단과대학 (colleges) */
@Entity
@Table(name = "colleges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "university_id", nullable = false)
    private Long universityId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "is_list_item", nullable = false)
    private Boolean isListItem = false;
}
