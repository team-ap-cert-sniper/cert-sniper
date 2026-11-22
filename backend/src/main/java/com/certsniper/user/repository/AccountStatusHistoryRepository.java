package com.certsniper.user.repository;

import com.certsniper.user.entity.AccountStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountStatusHistoryRepository extends JpaRepository<AccountStatusHistory, Long> {
}
