package com.mediator.cider.domain.user.service;

import com.mediator.cider.domain.user.dto.UserJoinRequest;
import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    /**
     * 회원가입 로직
     * @param request 회원가입 요청 DTO
     * @return 저장된 회원의 ID
     */
    @Transactional // 데이터 변경이 일어나므로 쓰기 권한 부여
    public Long join(UserJoinRequest request) {
        // 1. 이메일 중복 검증
        validateDuplicateEmail(request.getEmail());

        // 2. 비밀번호 암호화 (TODO: Spring Security 도입 시 BCrypt 적용 필요)
        // 지금은 임시로 클라이언트가 보낸 비밀번호를 그대로 사용합니다.
        String encodedPassword = request.getPassword();

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
}