package com.certsniper.qualification.repository;

import com.certsniper.qualification.entity.Qualification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualificationRepository extends JpaRepository<Qualification, String> {
}
