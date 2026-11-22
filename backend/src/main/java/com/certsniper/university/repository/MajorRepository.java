package com.certsniper.university.repository;

import com.certsniper.university.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorRepository extends JpaRepository<Major, Long> {
}
