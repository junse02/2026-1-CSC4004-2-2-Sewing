package com.mediator.cider.domain.user.controller;

import com.mediator.cider.domain.user.dto.UserJoinRequest;
import com.mediator.cider.domain.user.dto.UserLoginRequest;
import com.mediator.cider.domain.user.dto.UserProfileResponse;
import com.mediator.cider.domain.user.dto.UserProfileUpdateRequest;
import com.mediator.cider.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 관련 API 요청을 처리하는 컨트롤러
 * 작성자: 성준서
 */
@Tag(name = "user-controller", description = "회원 가입, 로그인 및 마이페이지 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 API
     * @param request 회원정보 (JSON 바디)
     * @return 생성된 회원의 ID와 함께 200 OK 응답
     */
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody UserJoinRequest request) {
        Long userId = userService.join(request);
        return ResponseEntity.ok(userId);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequest request) {
        String token = userService.login(request); // 여기서 이제 토큰 문자열이 넘어옵니다.
        return ResponseEntity.ok(token);
    }

    /**
     * 내 프로필(마이페이지) 조회 API
     * @param authentication 현재 인증된 사용자의 정보
     * @return 닉네임, 성별, MBTI, 애착유형, 가입일 등이 포함된 프로필 DTO 반환
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getMyInfo(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        UserProfileResponse profile = userService.getMyProfile(email);
        return ResponseEntity.ok(profile);
    }

    /**
     * 마이페이지 프로필 수정 API (애착유형 제외)
     * @param authentication 현재 인증된 사용자의 정보
     * @param request 수정할 데이터 (닉네임, 성별, MBTI)
     * @return 수정이 반영된 전체 프로필 정보
     */
    @PatchMapping("/profile-edit")
    public ResponseEntity<UserProfileResponse> updateMyInfo(
            Authentication authentication,
            @RequestBody UserProfileUpdateRequest request) {
            
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        UserProfileResponse updatedProfile = userService.updateMyProfile(email, request);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * 회원탈퇴 API
     * @param authentication 현재 인증된 사용자의 정보
     * @return 탈퇴 성공 메시지와 함께 200 OK 응답
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
        }
        
        // JWT 필터 등에서 저장한 사용자 식별자(이메일)를 가져옴
        String email = authentication.getName();
        
        userService.deleteUser(email);
        return ResponseEntity.ok("회원탈퇴가 성공적으로 완료되었습니다.");
    }
}
