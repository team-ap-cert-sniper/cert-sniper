package com.certsniper.user.service;

import com.certsniper.auth.dto.RegisterRequest;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.recommendation.dto.RecommendationRequest;
import com.certsniper.user.dto.UserProfileResponse;
import com.certsniper.user.dto.UserUpdateRequest;
import com.certsniper.user.entity.User;
import com.certsniper.user.repository.UserCertificateRepository;
import com.certsniper.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserCertificateRepository userCertificateRepository;

    public UserProfileResponse getMyProfile(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public UserProfileResponse updateMyProfile(Long userId, UserUpdateRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** auth 회원가입: 회원·학력·보유 자격 저장 후 회원 ID 반환 */
    @Transactional
    public Long createUser(RegisterRequest request, String encodedPassword) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** auth 로그인·비밀번호 재설정: 이메일로 회원 조회 (없으면 null) */
    public User findByEmail(String email) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
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
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
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
