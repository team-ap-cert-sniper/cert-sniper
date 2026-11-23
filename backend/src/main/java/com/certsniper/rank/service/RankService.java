package com.certsniper.rank.service;

import com.certsniper.passstory.repository.PassStoryRepository;
import com.certsniper.rank.dto.RankResponse;
import com.certsniper.rank.dto.RankUpResponse;
import com.certsniper.rank.entity.UserRank;
import com.certsniper.rank.repository.UserRankRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankService {

    /** 낮은 등급 → 높은 등급. 인덱스가 곧 등급 순서 */
    private static final List<String> RANKS = List.of("TRAINEE", "APPRENTICE", "MARKSMAN", "LEGEND");
    /** RANKS 와 같은 순서의 최소 자격증 수 */
    private static final int[] MIN_COUNTS = {0, 1, 3, 5};

    private final UserRankRepository userRankRepository;
    private final PassStoryRepository passStoryRepository;

    static String rankFor(int count) {
        int i = MIN_COUNTS.length - 1;
        while (count < MIN_COUNTS[i]) {
            i--;
        }
        return RANKS.get(i);
    }

    public RankResponse getMyRank(Long userId) {
        UserRank row = userRankRepository.findById(userId).orElse(null);
        int count = row == null ? 0 : row.getValidCertCount();
        String current = row == null ? RANKS.get(0) : row.getCurrentRank();
        int next = RANKS.indexOf(current) + 1;
        if (next >= RANKS.size()) {
            return new RankResponse(current, count, null, null);
        }
        return new RankResponse(current, count, RANKS.get(next), MIN_COUNTS[next] - count);
    }

    /** user 회원정보: 현재 등급 */
    public String getCurrentRank(Long userId) {
        return userRankRepository.findById(userId).map(UserRank::getCurrentRank).orElse(RANKS.get(0));
    }

    /** passstory 등록·삭제, admin 숨김 확정·재검토 후: 등급 다시 계산 */
    @Transactional
    public RankUpResponse recalculate(Long userId) {
        int count = passStoryRepository.countValidCerts(userId);
        UserRank row = userRankRepository.findById(userId).orElseGet(() -> new UserRank(userId));
        String previous = row.getCurrentRank();
        String current = rankFor(count);
        row.update(count, current);
        userRankRepository.save(row);
        boolean up = RANKS.indexOf(current) > RANKS.indexOf(previous);
        return new RankUpResponse(up, previous, current);
    }

    /** passstory·admin 목록: 작성자 등급 (회원 ID → 등급) */
    public Map<Long, String> getCurrentRanks(Collection<Long> userIds) {
        Map<Long, String> result = new HashMap<>();
        userIds.forEach(id -> result.put(id, RANKS.get(0)));
        userRankRepository.findAllById(userIds).forEach(r -> result.put(r.getUserId(), r.getCurrentRank()));
        return result;
    }
}
