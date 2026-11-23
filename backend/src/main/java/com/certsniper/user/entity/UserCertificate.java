package com.certsniper.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 보유 자격증 (user_certificates) */
@Entity
@Table(name = "user_certificates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "jmcd", length = 20)
    private String jmcd;

    @Column(name = "cert_name", length = 100)
    private String certName;

    public static UserCertificate ofJmcd(Long userId, String jmcd) {
        UserCertificate c = new UserCertificate();
        c.userId = userId;
        c.jmcd = jmcd;
        return c;
    }

    public static UserCertificate ofName(Long userId, String certName) {
        UserCertificate c = new UserCertificate();
        c.userId = userId;
        c.certName = certName;
        return c;
    }
}
