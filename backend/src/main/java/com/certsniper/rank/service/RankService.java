package com.certsniper.rank.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.rank.dto.RankResponse;
import com.certsniper.rank.dto.RankUpResponse;
import com.certsniper.rank.repository.UserRankRepository;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankService {

    private final UserRankRepository userRankRepository;

    public RankResponse getMyRank(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** user 회원정보: 현재 등급 */
    public String getCurrentRank(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** passstory 등록·삭제, admin 숨김 확정·재검토 후: 등급 다시 계산 */
    @Transactional
    public RankUpResponse recalculate(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** passstory·admin 목록: 작성자 등급 (회원 ID → 등급) */
    public Map<Long, String> getCurrentRanks(Collection<Long> userIds) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
