package com.certsniper.global.security;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import com.certsniper.user.entity.User;
import com.certsniper.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authorization: Bearer 토큰을 확인해 로그인 회원을 등록한다.
 * 토큰이 잘못되면 비회원으로 처리하고, 회원 전용 API 에서 INVALID_TOKEN(401)으로 응답한다.
 * 정지 기간 중인 계정은 403(ACCOUNT_SUSPENDED).
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String INVALID_TOKEN_ATTRIBUTE = "jwt.invalid";
    private static final String BEARER = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final SecurityErrorHandler securityErrorHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER)) {
            try {
                JwtTokenProvider.TokenInfo info = jwtTokenProvider.parse(header.substring(BEARER.length()));
                User user = userRepository.findById(info.userId()).orElse(null);
                if (user == null || "WITHDRAWN".equals(user.getStatus())) {
                    request.setAttribute(INVALID_TOKEN_ATTRIBUTE, true);
                } else if (isSuspended(user)) {
                    securityErrorHandler.write(response, ErrorCode.ACCOUNT_SUSPENDED);
                    return;
                } else {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (BusinessException e) {
                request.setAttribute(INVALID_TOKEN_ATTRIBUTE, true);
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isSuspended(User user) {
        return "SUSPENDED".equals(user.getStatus())
                && (user.getSuspendedUntil() == null || user.getSuspendedUntil().isAfter(DateUtils.now()));
    }
}
