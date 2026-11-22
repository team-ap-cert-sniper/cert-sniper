package com.certsniper.passstory.repository;

import com.certsniper.passstory.entity.PassStory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassStoryRepository extends JpaRepository<PassStory, Long> {
}
