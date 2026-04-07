package com.mediator.cider.domain.user.controller;

import com.mediator.cider.domain.user.dto.UserJoinRequest;
import com.mediator.cider.domain.user.dto.UserLoginRequest;
import com.mediator.cider.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Long> login(@RequestBody UserLoginRequest request) {
        Long userId = userService.login(request);
        return ResponseEntity.ok(userId);
    }
}