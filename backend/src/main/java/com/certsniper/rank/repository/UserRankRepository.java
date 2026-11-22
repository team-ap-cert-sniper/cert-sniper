package com.certsniper.rank.repository;

import com.certsniper.rank.entity.UserRank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRankRepository extends JpaRepository<UserRank, Long> {
}
