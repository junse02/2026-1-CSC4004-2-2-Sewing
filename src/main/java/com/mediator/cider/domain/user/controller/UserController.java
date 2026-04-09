package com.mediator.cider.domain.user.controller;

import com.mediator.cider.domain.user.dto.UserJoinRequest;
import com.mediator.cider.domain.user.dto.UserLoginRequest;
import com.mediator.cider.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 관련 API 요청을 처리하는 컨트롤러
 * 작성자: 성준서
 */
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
    @PostMapping("/join")
    public ResponseEntity<Long> join(@RequestBody UserJoinRequest request) {
        Long userId = userService.join(request);
        return ResponseEntity.ok(userId);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequest request) {
        String token = userService.login(request); // 여기서 이제 토큰 문자열이 넘어옵니다.
        return ResponseEntity.ok(token);
    }

    // TODO: 해당 코드 다듬을 필요 있음
    // jwt 필터 확인위해 만든 껍데기 기능
    @GetMapping("/me")
    public ResponseEntity<String> getMyInfo(org.springframework.security.core.Authentication authentication) {
        // 필터를 통과했다면 authentication 객체에 유저 정보(이메일)가 들어있습니다.
        if (authentication == null) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
        }
        return ResponseEntity.ok("현재 로그인한 유저: " + authentication.getName());
    }
}