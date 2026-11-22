package com.certsniper.global.config;

import com.certsniper.global.security.JwtAuthenticationFilter;
import com.certsniper.global.security.JwtTokenProvider;
import com.certsniper.global.security.SecurityErrorHandler;
import com.certsniper.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * URL별 접근 권한 설정.
 *
 *  PATCH /api/v1/auth/password  로그인 회원 (마이페이지 비밀번호 변경)
 *  /api/v1/auth/**          전체 허용
 *  GET /api/v1/options/**   전체 허용
 *  GET /api/v1/daily-idiom  전체 허용
 *  GET /api/v1/trends/**    전체 허용
 *  GET /api/v1/health       전체 허용
 *  Swagger                  전체 허용
 *  /api/v1/admin/**         ADMIN (403)
 *  그 밖의 /api/v1/**       로그인 회원 (401)
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    static final String[] LOGIN_REQUIRED_PATCH = {
            "/api/v1/auth/password"
    };

    static final String[] PUBLIC_ANY = {
            "/api/v1/auth/**",
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
    };

    static final String[] PUBLIC_GET = {
            "/api/v1/health",
            "/api/v1/options/**",
            "/api/v1/daily-idiom",
            "/api/v1/trends/**"
    };

    private final SecurityErrorHandler securityErrorHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()   // CORS preflight
                        .requestMatchers(HttpMethod.PATCH, LOGIN_REQUIRED_PATCH).authenticated()
                        .requestMatchers(PUBLIC_ANY).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().denyAll()
                )

                // 401·403 응답
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler)
                );

        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider, userRepository, securityErrorHandler),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 비밀번호 BCrypt 해시 (AUTH-01) */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
