package com.certsniper.user.service;

import com.certsniper.auth.dto.RegisterRequest;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import com.certsniper.option.dto.SchoolInfoResponse;
import com.certsniper.option.service.OptionService;
import com.certsniper.qualification.service.QualificationService;
import com.certsniper.rank.service.RankService;
import com.certsniper.recommendation.dto.RecommendationRequest;
import com.certsniper.user.dto.UserProfileResponse;
import com.certsniper.user.dto.UserUpdateRequest;
import com.certsniper.user.entity.AccountStatusHistory;
import com.certsniper.user.entity.User;
import com.certsniper.user.entity.UserCertificate;
import com.certsniper.user.repository.AccountStatusHistoryRepository;
import com.certsniper.user.repository.UserCertificateRepository;
import com.certsniper.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final int MAX_CERT_NAME_LENGTH = 100;

    private final UserRepository userRepository;
    private final UserCertificateRepository userCertificateRepository;
    private final AccountStatusHistoryRepository accountStatusHistoryRepository;
    private final RankService rankService;
    private final OptionService optionService;
    private final QualificationService qualificationService;

    public UserProfileResponse getMyProfile(Long userId) {
        User u = getUser(userId);
        SchoolInfoResponse school = u.getUniversityId() == null
                ? null
                : optionService.getSchoolNames(u.getUniversityId(), u.getCollegeId(), u.getMajorId());
        List<UserProfileResponse.OwnedCertificate> owned = userCertificateRepository.findByUserId(userId).stream()
                .map(c -> new UserProfileResponse.OwnedCertificate(
                        c.getJmcd(),
                        c.getJmcd() == null ? null : qualificationService.getInfo(c.getJmcd()).jmfldnm(),
                        c.getCertName()))
                .toList();
        return new UserProfileResponse(
                u.getId(), u.getEmail(), u.getName(), u.getNickname(), u.getRole(), u.getStatus(),
                u.getUniversityId(), school == null ? null : school.universityName(),
                u.getCollegeId(), u.getMajorId(), school == null ? null : school.majorName(),
                u.getGrade(), owned, rankService.getCurrentRank(userId), u.getWithdrawalRequestedAt());
    }

    /** 이메일은 바꿀 수 없음. 보유 자격은 요청 목록으로 통째로 교체 */
    @Transactional
    public UserProfileResponse updateMyProfile(Long userId, UserUpdateRequest request) {
        User u = getUser(userId);
        if (!u.getNickname().equals(request.nickname()) && !isNicknameAvailable(request.nickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE);
        }
        optionService.validateSchoolInfo(request.universityId(), request.collegeId(), request.majorId(), request.grade());
        List<String> jmcds = distinctNonBlank(request.ownedJmcds());
        jmcds.forEach(qualificationService::getInfo); // 없는 종목이면 NOT_FOUND
        List<String> certNames = certNames(request.ownedCertNames());

        u.changeNickname(request.nickname());
        u.changeSchool(request.universityId(), request.collegeId(), request.majorId(), request.grade());
        userCertificateRepository.deleteAllByUserId(userId);
        saveCertificates(userId, jmcds, certNames);
        return getMyProfile(userId);
    }

    /** auth 회원가입: 회원·학력·보유 자격 저장 후 회원 ID 반환 */
    @Transactional
    public Long createUser(RegisterRequest request, String encodedPassword) {
        // 학교 정보 검증(OptionService.validateSchoolInfo)은 auth가 호출한다. 닉네임만 한 번 더 확인
        if (!isNicknameAvailable(request.nickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE);
        }
        User user = userRepository.save(User.create(
                request.email(), encodedPassword, request.name(), request.nickname(),
                request.universityId(), request.collegeId(), request.majorId(), request.grade()));
        saveCertificates(user.getId(), distinctNonBlank(request.ownedJmcds()), certNames(request.ownedCertNames()));
        return user.getId();
    }

    private void saveCertificates(Long userId, List<String> jmcds, List<String> certNames) {
        List<UserCertificate> certs = new ArrayList<>();
        jmcds.forEach(j -> certs.add(UserCertificate.ofJmcd(userId, j)));
        certNames.forEach(n -> certs.add(UserCertificate.ofName(userId, n)));
        userCertificateRepository.saveAll(certs);
    }

    private static List<String> distinctNonBlank(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().filter(v -> v != null && !v.isBlank()).map(String::trim).distinct().toList();
    }

    private static List<String> certNames(List<String> values) {
        List<String> names = distinctNonBlank(values);
        if (names.stream().anyMatch(n -> n.length() > MAX_CERT_NAME_LENGTH)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return names;
    }

    /** auth 로그인·비밀번호 재설정: 이메일로 회원 조회 (없으면 null) */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /** 회원 조회 (없으면 NOT_FOUND) */
    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    /** auth 비밀번호 재설정·변경: 암호화된 비밀번호 저장 */
    @Transactional
    public void updatePassword(Long userId, String encodedPassword) {
        getUser(userId).changePassword(encodedPassword);
    }

    /** auth 닉네임 중복 확인 */
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    /** recommendation 전공 맞춤 추천: 회원정보의 학력·보유 자격 */
    public RecommendationRequest getRecommendationProfile(Long userId) {
        User u = getUser(userId);
        List<String> owned = userCertificateRepository.findByUserId(userId).stream()
                .map(UserCertificate::getJmcd)
                .filter(Objects::nonNull)
                .toList();
        return new RecommendationRequest(u.getUniversityId(), u.getMajorId(), u.getGrade(), owned);
    }

    /** admin 대시보드: 전체 회원 수 */
    public long countUsers() {
        return userRepository.count();
    }

    /** auth 로그인: 정지 기간이 끝났으면 ACTIVE로 되돌리고 이력 기록 */
    @Transactional
    public void releaseExpiredSuspension(Long userId) {
        User u = getUser(userId);
        LocalDateTime until = u.getSuspendedUntil();
        if ("SUSPENDED".equals(u.getStatus()) && until != null && !until.isAfter(DateUtils.now())) {
            u.activate();
            accountStatusHistoryRepository.save(AccountStatusHistory.of(
                    userId, "AUTO_UNSUSPEND", "SUSPENDED", "ACTIVE", null, null, null));
        }
    }

    /** passstory·admin 목록: 작성자 닉네임 (회원 ID → 닉네임) */
    public Map<Long, String> getNicknames(Collection<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));
    }

    /** admin 신고 숨김 확정: 누적 숨김 횟수 +1 (정지는 관리자가 suspend로 직접 실행) */
    @Transactional
    public void addConfirmedHidden(Long userId) {
        getUser(userId).addConfirmedHidden();
    }

    /** admin 회원 목록 검색: keyword는 이메일·이름·닉네임 부분 일치 (빈 값이면 전체) */
    public Page<User> searchUsers(String keyword, String status, Pageable pageable) {
        String k = keyword == null || keyword.isBlank() ? null : keyword.trim();
        String s = status == null || status.isBlank() ? null : status;
        return userRepository.search(k, s, pageable);
    }

    /** admin 회원 정보 정정: 이름·닉네임은 null이면 유지, 학력은 묶음으로 교체 (넷 다 null = 학교 정보 없음) */
    @Transactional
    public void correctProfile(Long userId, String name, String nickname, Long universityId, Long collegeId, Long majorId, String grade) {
        User u = getUser(userId);
        if (nickname != null && !nickname.equals(u.getNickname())) {
            if (!isNicknameAvailable(nickname)) {
                throw new BusinessException(ErrorCode.DUPLICATE);
            }
            u.changeNickname(nickname);
        }
        if (name != null) {
            u.changeName(name);
        }
        optionService.validateSchoolInfo(universityId, collegeId, majorId, grade);
        u.changeSchool(universityId, collegeId, majorId, grade);
    }

    /** admin 계정 정지 (이력 기록). 정지 중이면 기간을 새로 덮어씀 */
    @Transactional
    public void suspend(Long userId, LocalDateTime suspendedUntil, String reason, Long adminId) {
        if (suspendedUntil == null || !suspendedUntil.isAfter(DateUtils.now()) || reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        User u = getUser(userId);
        String previous = u.getStatus();
        if (!"ACTIVE".equals(previous) && !"SUSPENDED".equals(previous)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        u.suspend(suspendedUntil);
        accountStatusHistoryRepository.save(AccountStatusHistory.of(
                userId, "SUSPEND", previous, "SUSPENDED", reason, suspendedUntil, adminId));
    }

    /** admin 계정 정지 해제 (이력 기록). DB CHECK상 UNSUSPEND는 사유가 필수라 고정 문구를 남긴다 */
    @Transactional
    public void unsuspend(Long userId, Long adminId) {
        User u = getUser(userId);
        if (!"SUSPENDED".equals(u.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        u.activate();
        accountStatusHistoryRepository.save(AccountStatusHistory.of(
                userId, "UNSUSPEND", "SUSPENDED", "ACTIVE", "관리자 정지 해제", null, adminId));
    }
}
