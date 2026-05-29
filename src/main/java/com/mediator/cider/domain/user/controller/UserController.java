package com.mediator.cider.domain.user.controller;

import com.mediator.cider.domain.user.dto.*;
import com.mediator.cider.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "user-controller", description = "회원 가입, 로그인 및 마이페이지 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody UserJoinRequest request) {
        Long userId = userService.join(request);
        return ResponseEntity.ok(userId);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequest request) {
        String token = userService.login(request);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getMyInfo(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        UserProfileResponse profile = userService.getMyProfile(email);
        return ResponseEntity.ok(profile);
    }

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

    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
        }
        
        String email = authentication.getName();
        
        userService.deleteUser(email);
        return ResponseEntity.ok("회원탈퇴가 성공적으로 완료되었습니다.");
    }

    @GetMapping("/my-stats")
    public ResponseEntity<MyPageStatsResponse> getMyStats(Authentication authentication) {
        String email = authentication.getName();
        MyPageStatsResponse stats = userService.getMyPageStats(email);
        return ResponseEntity.ok(stats);
    }
}
