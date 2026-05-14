package com.mediator.cider.domain.user.controller;

import com.mediator.cider.domain.user.dto.FriendAddRequest;
import com.mediator.cider.domain.user.dto.FriendResponse;
import com.mediator.cider.domain.user.service.FriendService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "friend-controller", description = "친구 추가, 목록 조회 및 삭제 API")
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /**
     * 친구 코드로 친구 추가
     */
    @PostMapping("/add")
    public ResponseEntity<String> addFriend(
            Authentication authentication,
            @RequestBody FriendAddRequest request) {
            
        String email = authentication.getName();
        String result = friendService.addFriend(email, request.getFriendCode());
        return ResponseEntity.ok(result);
    }

    /**
     * 내 친구 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<List<FriendResponse>> getMyFriends(Authentication authentication) {
        String email = authentication.getName();
        List<FriendResponse> friends = friendService.getMyFriends(email);
        return ResponseEntity.ok(friends);
    }

    /**
     * 친구 삭제 (쌍방향 삭제)
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<String> deleteFriend(
            Authentication authentication,
            @PathVariable Long friendId) {
            
        String email = authentication.getName();
        String result = friendService.deleteFriend(email, friendId);
        return ResponseEntity.ok(result);
    }
}
