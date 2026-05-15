package com.mediator.cider.domain.user.service;

import com.mediator.cider.domain.attachment.entity.UserAttachment;
import com.mediator.cider.domain.attachment.repository.UserAttachmentRepository;
import com.mediator.cider.domain.user.dto.UserJoinRequest;
import com.mediator.cider.domain.user.dto.UserLoginRequest;
import com.mediator.cider.domain.user.dto.UserProfileResponse;
import com.mediator.cider.domain.user.dto.UserProfileUpdateRequest;
import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.domain.user.repository.UserRepository;
import com.mediator.cider.global.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 회원 관련 비즈니스 로직을 처리하는 서비스
 * 작성자: 성준서
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 조회 성능 최적화
public class UserService {

    private final UserRepository userRepository;
    private final UserAttachmentRepository userAttachmentRepository; // 애착유형 조회를 위해 추가
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

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

        // 3. 고유 친구 코드 8자리 생성 (중복 체크 생략: 확률적으로 매우 낮음)
        String friendCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 4. DTO -> Entity 변환 및 저장
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .gender(request.getGender())
                .mbti(request.getMbti())
                .friendCode(friendCode)
                .build();
                
        User savedUser = userRepository.save(user);

        return savedUser.getId();
    }

    /**
     * 중복 이메일 확인 전용 메서드
     */
    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
    }

    /**
     * 로그인 로직
     * @return 로그인 성공 시 유저 ID (나중에 토큰이나 세션으로 발전시킬 예정)
     */
    public String login(UserLoginRequest request) {
        // 1. 이메일로 유저 찾기
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 2. 비밀번호 일치 확인 (평문 비번, 암호화된 비번)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return jwtProvider.createToken(user.getEmail());
    }

    /**
     * 내 프로필(마이페이지) 조회 로직
     * @param email 조회할 회원의 이메일
     * @return 유저 프로필 정보 DTO
     */
    public UserProfileResponse getMyProfile(String email) {
        // 1. 유저 기본 정보 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 유저 애착유형 정보 조회 (없을 수도 있음)
        UserAttachment attachment = userAttachmentRepository.findByUserId(user.getId())
                .orElse(null);

        // 3. 응답용 DTO 생성 후 반환
        return UserProfileResponse.of(user, attachment != null ? attachment.getType() : null);
    }

    /**
     * 내 프로필 수정 로직 (애착 유형은 제외)
     * @param email 수정할 회원의 이메일
     * @param request 수정할 데이터 (닉네임, 성별, MBTI)
     * @return 수정이 완료된 프로필 응답 DTO
     */
    @Transactional
    public UserProfileResponse updateMyProfile(String email, UserProfileUpdateRequest request) {
        // 1. 유저 찾기
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 프로필 정보 업데이트 (JPA Dirty Checking 덕분에 save() 생략 가능)
        user.updateProfile(request.getNickname(), request.getGender(), request.getMbti());

        // 3. 업데이트된 정보를 기반으로 DTO 반환 (애착 유형은 변하지 않으므로 기존 로직 재활용)
        UserAttachment attachment = userAttachmentRepository.findByUserId(user.getId())
                .orElse(null);
                
        return UserProfileResponse.of(user, attachment != null ? attachment.getType() : null);
    }

    /**
     * 회원탈퇴 로직
     * @param email 탈퇴할 회원의 이메일 (인증된 유저 정보 기반)
     */
    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        
        // Soft delete 처리
        user.delete();
    }
}
