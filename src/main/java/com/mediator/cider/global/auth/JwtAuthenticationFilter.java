package com.mediator.cider.global.auth;

import com.mediator.cider.global.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 토큰 필터링 로직 구현한 클래스
 * 작성자: 성준서
 */

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 "Authorization" 항목을 꺼냅니다.
        String authHeader = request.getHeader("Authorization");

        // 2. 헤더가 "Bearer "로 시작하는지 확인합니다.
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // "Bearer " 뒷부분인 토큰만 추출

            // 3. 토큰이 유효한지 JwtProvider로 검증합니다.
            if (jwtProvider.validateToken(token)) {
                String email = jwtProvider.getSubject(token); // 토큰에서 이메일 추출

                // 4. 인증이 완료되면 스프링 시큐리티의 '인증 바구니(Context)'에 유저 정보를 담습니다.
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 5. 다음 필터로 요청을 넘깁니다.
        filterChain.doFilter(request, response);
    }
}
