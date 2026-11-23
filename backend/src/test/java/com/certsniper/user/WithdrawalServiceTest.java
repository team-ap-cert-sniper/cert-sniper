package com.certsniper.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.certsniper.auth.service.AuthService;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.user.dto.WithdrawRequest;
import com.certsniper.user.entity.User;
import com.certsniper.user.repository.AccountStatusHistoryRepository;
import com.certsniper.user.repository.UserRepository;
import com.certsniper.user.service.WithdrawalService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class WithdrawalServiceTest {

    private final UserRepository userRepo = mock(UserRepository.class);
    private final AccountStatusHistoryRepository historyRepo = mock(AccountStatusHistoryRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final AuthService authService = mock(AuthService.class);
    private final WithdrawalService service = new WithdrawalService(userRepo, historyRepo, encoder, authService);

    private User stored() {
        User u = User.create("a@b.c", "hash", "홍길동", "길동", null, null, null, null);
        when(userRepo.findById(1L)).thenReturn(Optional.of(u));
        return u;
    }

    @Test
    void 비밀번호가_틀리면_탈퇴_안_됨() {
        User u = stored();
        when(encoder.matches("wrong", "hash")).thenReturn(false);
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.requestWithdrawal(1L, new WithdrawRequest("wrong")));
        assertEquals(ErrorCode.VALIDATION_ERROR, e.getErrorCode());
        assertEquals("ACTIVE", u.getStatus());
        verify(authService, never()).revokeAllTokens(anyLong());
    }

    @Test
    void 탈퇴_신청하면_대기_상태_이력_토큰_폐기() {
        User u = stored();
        when(encoder.matches("pw", "hash")).thenReturn(true);
        service.requestWithdrawal(1L, new WithdrawRequest("pw"));
        assertEquals("WITHDRAWAL_PENDING", u.getStatus());
        assertNotNull(u.getWithdrawalRequestedAt());
        verify(historyRepo).save(any());
        verify(authService).revokeAllTokens(1L);
    }

    @Test
    void 이미_탈퇴_대기면_DUPLICATE() {
        User u = stored();
        u.requestWithdrawal(LocalDateTime.now());
        assertEquals(ErrorCode.DUPLICATE, assertThrows(BusinessException.class,
                () -> service.requestWithdrawal(1L, new WithdrawRequest("pw"))).getErrorCode());
    }

    @Test
    void 철회하면_ACTIVE_대기중이_아니면_오류() {
        User u = stored();
        assertEquals(ErrorCode.VALIDATION_ERROR,
                assertThrows(BusinessException.class, () -> service.cancelWithdrawal(1L)).getErrorCode());
        u.requestWithdrawal(LocalDateTime.now());
        service.cancelWithdrawal(1L);
        assertEquals("ACTIVE", u.getStatus());
        assertNull(u.getWithdrawalRequestedAt());
    }
}
