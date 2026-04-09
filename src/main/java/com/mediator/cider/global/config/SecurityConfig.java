package com.mediator.cider.global.config;

import com.mediator.cider.global.JwtProvider;
import com.mediator.cider.global.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 클래스
 * 작성자: 성준서
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    // 비밀번호 암호화 도구 등록
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API 서버이므로 CSRF 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 X (JWT 방식)
                .authorizeHttpRequests(auth -> auth
                        // 로그인, 회원가입, Swagger는 로그인 토큰 없이도 가능핟로ㅗㄱ
                        .requestMatchers("/api/users/login", "/api/users/signup", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 그 외 모든 요청은 로그인이 되어야 가능
                        .anyRequest().authenticated()
                )
                // 🚩 핵심: UsernamePasswordAuthenticationFilter(기본 로그인 필터) 실행 전에
                // 우리가 만든 JWT 필터를 먼저 실행해라!
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}