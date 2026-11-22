package com.certsniper.user.repository;

import com.certsniper.user.entity.UserCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCertificateRepository extends JpaRepository<UserCertificate, Long> {
}
