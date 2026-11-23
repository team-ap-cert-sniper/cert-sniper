package com.certsniper.user.service;

import com.certsniper.auth.dto.RegisterRequest;
import com.certsniper.auth.service.AuthRules;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.recommendation.dto.RecommendationRequest;
import com.certsniper.user.dto.UserProfileResponse;
import com.certsniper.user.dto.UserUpdateRequest;
import com.certsniper.user.entity.User;
import com.certsniper.user.entity.UserCertificate;
import com.certsniper.user.repository.UserCertificateRepository;
import com.certsniper.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    public static final String DUPLICATE_EMAIL_MESSAGE = "이미 가입된 이메일입니다.";
    public static final String DUPLICATE_NICKNAME_MESSAGE = "이미 사용 중인 닉네임입니다.";

    private final UserRepository userRepository;
    private final UserCertificateRepository userCertificateRepository;

    public UserProfileResponse getMyProfile(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public UserProfileResponse updateMyProfile(Long userId, UserUpdateRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * auth 회원가입: 회원·학력·보유 자격 저장 후 회원 ID 반환.
     * 저장 직전에 이메일·닉네임 중복을 한 번 더 확인한다. 화면에서 중복 확인을 통과했더라도
     * 그사이 다른 사람이 먼저 가입했을 수 있기 때문이다. 겹치면 DUPLICATE(409)로 가입을 막는다.
     * 두 요청이 동시에 확인을 통과한 경우는 DB UNIQUE 제약(uq_users_1 이메일, uq_users_2 닉네임)이 막고,
     * 그 오류도 DUPLICATE로 바꿔 돌려준다.
     */
    @Transactional
    public Long createUser(RegisterRequest request, String encodedPassword) {
        String email = AuthRules.normalizeEmail(request.email());
        String nickname = AuthRules.normalizeNickname(request.nickname());

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE, DUPLICATE_EMAIL_MESSAGE);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE, DUPLICATE_NICKNAME_MESSAGE);
        }

        User user = User.create(
                email,
                encodedPassword,
                request.name().trim(),
                nickname,
                request.universityId(),
                request.collegeId(),
                request.majorId(),
                request.grade());
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            String detail = String.valueOf(e.getMostSpecificCause().getMessage());
            if (detail.contains("uq_users_1")) {
                throw new BusinessException(ErrorCode.DUPLICATE, DUPLICATE_EMAIL_MESSAGE);
            }
            if (detail.contains("uq_users_2")) {
                throw new BusinessException(ErrorCode.DUPLICATE, DUPLICATE_NICKNAME_MESSAGE);
            }
            throw e;
        }

        saveCertificates(user.getId(), request.ownedJmcds(), request.ownedCertNames());
        return user.getId();
    }

    /** 보유 자격증 저장 (빈 값·중복 제거) */
    private void saveCertificates(Long userId, List<String> jmcds, List<String> certNames) {
        Set<String> uniqueJmcds = new LinkedHashSet<>();
        if (jmcds != null) {
            jmcds.stream().filter(Objects::nonNull).map(String::trim).filter(v -> !v.isEmpty()).forEach(uniqueJmcds::add);
        }
        Set<String> uniqueNames = new LinkedHashSet<>();
        if (certNames != null) {
            certNames.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(v -> !v.isEmpty() && v.length() <= 100)
                    .forEach(uniqueNames::add);
        }
        List<UserCertificate> certificates = new ArrayList<>();
        uniqueJmcds.forEach(jmcd -> certificates.add(UserCertificate.ofJmcd(userId, jmcd)));
        uniqueNames.forEach(name -> certificates.add(UserCertificate.ofCertName(userId, name)));
        if (!certificates.isEmpty()) {
            userCertificateRepository.saveAll(certificates);
        }
    }

    /** auth 로그인·비밀번호 재설정: 이메일로 회원 조회 (없으면 null) */
    public User findByEmail(String email) {
        return userRepository.findByEmail(AuthRules.normalizeEmail(email)).orElse(null);
    }

    /** 회원 조회 (없으면 NOT_FOUND) */
    public User getUser(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** auth 비밀번호 재설정·변경: 암호화된 비밀번호 저장 */
    @Transactional
    public void updatePassword(Long userId, String encodedPassword) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** auth 닉네임 중복 확인 */
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(AuthRules.normalizeNickname(nickname));
    }

    /** recommendation 전공 맞춤 추천: 회원정보의 학력·보유 자격 */
    public RecommendationRequest getRecommendationProfile(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** admin 대시보드: 전체 회원 수 */
    public long countUsers() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** auth 로그인: 정지 기간이 끝났으면 ACTIVE로 되돌리고 이력 기록 */
    @Transactional
    public void releaseExpiredSuspension(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** passstory·admin 목록: 작성자 닉네임 (회원 ID → 닉네임) */
    public Map<Long, String> getNicknames(Collection<Long> userIds) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** admin 신고 숨김 확정: 누적 숨김 횟수 +1 (정지는 관리자가 suspend로 직접 실행) */
    @Transactional
    public void addConfirmedHidden(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** admin 회원 목록 검색 */
    public Page<User> searchUsers(String keyword, String status, Pageable pageable) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** admin 회원 정보 정정 */
    @Transactional
    public void correctProfile(Long userId, String name, String nickname, Long universityId, Long collegeId, Long majorId, String grade) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** admin 계정 정지 (이력 기록) */
    @Transactional
    public void suspend(Long userId, LocalDateTime suspendedUntil, String reason, Long adminId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** admin 계정 정지 해제 (이력 기록) */
    @Transactional
    public void unsuspend(Long userId, Long adminId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}