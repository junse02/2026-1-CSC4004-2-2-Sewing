package com.mediator.cider.domain.user.service;

import com.mediator.cider.domain.user.dto.UserJoinRequest;
import com.mediator.cider.domain.user.dto.UserLoginRequest;
import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 관련 비즈니스 로직을 처리하는 서비스
 * 작성자: 성준서
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 조회 성능 최적화
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 회원가입 로직
     * @param request 회원가입 요청 DTO
     * @return 저장된 회원의 ID
     */
    @Transactional // 데이터 변경이 일어나므로 쓰기 권한 부여
    public Long join(UserJoinRequest request) {
        // 1. 이메일 중복 검증
        validateDuplicateEmail(request.getEmail());

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. DTO -> Entity 변환 및 저장
        User user = request.toEntity(encodedPassword);
        User savedUser = userRepository.save(user);

        return savedUser.getId();
    }

    /**
     * 중복 이메일 확인 전용 메서드
     */
    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
    }

    /**
     * 로그인 로직
     * @return 로그인 성공 시 유저 ID (나중에 토큰이나 세션으로 발전시킬 예정)
     */
    public Long login(UserLoginRequest request) {
        // 1. 이메일로 유저 찾기
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 2. 비밀번호 일치 확인 (평문 비번, 암호화된 비번)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user.getId();
    }
}