package com.certsniper.job.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 관심 분야 (interest_fields) */
@Entity
@Table(name = "interest_fields")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterestField {

    @Id
    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "name", nullable = false, length = 50)
    private String name;
}
