package com.certsniper.favorite.repository;

import com.certsniper.favorite.entity.Favorite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndJmcd(Long userId, String jmcd);

    List<Favorite> findByUserId(Long userId);

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserIdAndJmcd(Long userId, String jmcd);

    void deleteByUserId(Long userId);
}
