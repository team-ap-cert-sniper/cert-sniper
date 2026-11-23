package com.certsniper.user.entity;

import com.certsniper.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 회원 (users) */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "name", nullable = false, length = 4)
    private String name;

    @Column(name = "nickname", nullable = false, length = 10)
    private String nickname;

    @Column(name = "role", nullable = false, length = 10)
    private String role = "USER";

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "university_id")
    private Long universityId;

    @Column(name = "college_id")
    private Long collegeId;

    @Column(name = "major_id")
    private Long majorId;

    @Column(name = "grade", length = 15)
    private String grade;

    @Column(name = "confirmed_hidden_count", nullable = false)
    private Integer confirmedHiddenCount = 0;

    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @Column(name = "withdrawal_requested_at")
    private LocalDateTime withdrawalRequestedAt;

    public static User create(String email, String encodedPassword, String name, String nickname,
            Long universityId, Long collegeId, Long majorId, String grade) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.name = name;
        user.nickname = nickname;
        user.universityId = universityId;
        user.collegeId = collegeId;
        user.majorId = majorId;
        user.grade = grade;
        return user;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
