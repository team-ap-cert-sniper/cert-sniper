package com.certsniper.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import com.certsniper.option.service.OptionService;
import com.certsniper.qualification.service.QualificationService;
import com.certsniper.rank.service.RankService;
import com.certsniper.user.dto.UserUpdateRequest;
import com.certsniper.user.entity.User;
import com.certsniper.user.repository.AccountStatusHistoryRepository;
import com.certsniper.user.repository.UserCertificateRepository;
import com.certsniper.user.repository.UserRepository;
import com.certsniper.user.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private final UserRepository userRepo = mock(UserRepository.class);
    private final AccountStatusHistoryRepository historyRepo = mock(AccountStatusHistoryRepository.class);
    private final UserService service = new UserService(userRepo, mock(UserCertificateRepository.class), historyRepo,
            mock(RankService.class), mock(OptionService.class), mock(QualificationService.class));

    private User stored() {
        User u = User.create("a@b.c", "hash", "홍길동", "길동", null, null, null, null);
        when(userRepo.findById(1L)).thenReturn(Optional.of(u));
        return u;
    }

    @Test
    void 정지_기간이_끝났으면_로그인_때_자동_해제() {
        User u = stored();
        u.suspend(DateUtils.now().minusMinutes(1));
        service.releaseExpiredSuspension(1L);
        assertEquals("ACTIVE", u.getStatus());
        verify(historyRepo).save(any());
    }

    @Test
    void 정지_기간이_남았으면_유지() {
        User u = stored();
        u.suspend(DateUtils.now().plusDays(3));
        service.releaseExpiredSuspension(1L);
        assertEquals("SUSPENDED", u.getStatus());
        verify(historyRepo, never()).save(any());
    }

    @Test
    void 과거_날짜나_사유_없이_정지하면_VALIDATION_ERROR() {
        stored();
        assertEquals(ErrorCode.VALIDATION_ERROR, assertThrows(BusinessException.class,
                () -> service.suspend(1L, DateUtils.now().minusDays(1), "사유", 9L)).getErrorCode());
        assertEquals(ErrorCode.VALIDATION_ERROR, assertThrows(BusinessException.class,
                () -> service.suspend(1L, DateUtils.now().plusDays(3), " ", 9L)).getErrorCode());
    }

    @Test
    void 정지_중이_아니면_해제_불가() {
        stored();
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(BusinessException.class, () -> service.unsuspend(1L, 9L)).getErrorCode());
    }

    @Test
    void 다른_회원이_쓰는_닉네임으로_변경하면_DUPLICATE() {
        stored();
        when(userRepo.existsByNickname("남의닉")).thenReturn(true);
        UserUpdateRequest req = new UserUpdateRequest("남의닉", null, null, null, null, null, null);
        assertEquals(ErrorCode.DUPLICATE,
                assertThrows(BusinessException.class, () -> service.updateMyProfile(1L, req)).getErrorCode());
    }
}
