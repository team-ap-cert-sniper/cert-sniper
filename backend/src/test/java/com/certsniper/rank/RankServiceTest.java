package com.certsniper.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.certsniper.passstory.repository.PassStoryRepository;
import com.certsniper.rank.dto.RankUpResponse;
import com.certsniper.rank.entity.UserRank;
import com.certsniper.rank.repository.UserRankRepository;
import com.certsniper.rank.service.RankService;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class RankServiceTest {

    private final UserRankRepository rankRepo = mock(UserRankRepository.class);
    private final PassStoryRepository storyRepo = mock(PassStoryRepository.class);
    private final RankService service = new RankService(rankRepo, storyRepo);

    private RankUpResponse recalc(int validCount, UserRank existing) {
        when(storyRepo.countValidCerts(1L)).thenReturn(validCount);
        when(rankRepo.findById(1L)).thenReturn(Optional.ofNullable(existing));
        return service.recalculate(1L);
    }

    @ParameterizedTest
    @CsvSource({"0,TRAINEE", "1,APPRENTICE", "2,APPRENTICE", "3,MARKSMAN", "4,MARKSMAN", "5,LEGEND", "9,LEGEND"})
    void 자격증_수별_등급_경계(int count, String expected) {
        assertEquals(expected, recalc(count, null).newRank());
    }

    @Test
    void 첫_합격수기_등록은_승급() {
        RankUpResponse r = recalc(1, null);
        assertTrue(r.rankedUp());
        assertEquals("TRAINEE", r.previousRank());
        assertEquals("APPRENTICE", r.newRank());
    }

    @Test
    void 같은_등급이면_승급_아님() {
        UserRank row = new UserRank(1L);
        row.update(1, "APPRENTICE");
        assertFalse(recalc(2, row).rankedUp());
    }

    @Test
    void 등급이_내려가도_승급_아님() {
        UserRank row = new UserRank(1L);
        row.update(3, "MARKSMAN");
        RankUpResponse r = recalc(2, row);
        assertFalse(r.rankedUp());
        assertEquals("APPRENTICE", r.newRank());
    }
}
