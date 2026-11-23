package com.certsniper.favorite.service;

import com.certsniper.favorite.dto.FavoriteResponse;
import com.certsniper.favorite.entity.Favorite;
import com.certsniper.favorite.repository.FavoriteRepository;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.qualification.dto.QualificationDetailResponse.QualificationInfo;
import com.certsniper.qualification.service.QualificationService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final QualificationService qualificationService;

    /** 최근 찜한 순 */
    public List<FavoriteResponse> getFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(f -> {
                    QualificationInfo q = qualificationService.getInfo(f.getJmcd());
                    return new FavoriteResponse(f.getJmcd(), q.jmfldnm(), q.qualgbnm(), q.gradeLevel(), q.summary(),
                            f.getCreatedAt());
                })
                .toList();
    }

    @Transactional
    public void addFavorite(Long userId, String jmcd) {
        if (favoriteRepository.existsByUserIdAndJmcd(userId, jmcd)) {
            throw new BusinessException(ErrorCode.DUPLICATE);
        }
        favoriteRepository.save(Favorite.of(userId, jmcd));
    }

    /** 찜하지 않은 종목이어도 오류 없이 204 (해제 결과가 같으므로) */
    @Transactional
    public void removeFavorite(Long userId, String jmcd) {
        favoriteRepository.deleteByUserIdAndJmcd(userId, jmcd);
    }

    /** recommendation 카드: 찜한 종목코드 */
    public Set<String> getFavoriteJmcds(Long userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(Favorite::getJmcd)
                .collect(Collectors.toSet());
    }

    /** user 탈퇴 확정: 찜 전체 삭제 */
    @Transactional
    public void deleteAllByUser(Long userId) {
        favoriteRepository.deleteByUserId(userId);
    }
}
