package com.mediator.cider.domain.mediation.controller;

import com.mediator.cider.domain.mediation.dto.MediationRecordRequest;
import com.mediator.cider.domain.mediation.dto.MediationSessionResponse;
import com.mediator.cider.domain.mediation.service.MediationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "sewing-controller", description = "갈등 중재(sewing) 관련 API")
@RestController
@RequestMapping("/api/sewings")
@RequiredArgsConstructor
public class MediationController {

    private final MediationService mediationService;

    /**
     * 갈등 중재 방 생성
     */
    @PostMapping
    public ResponseEntity<String> createSession(Authentication authentication) {
        String email = authentication.getName();
        Long sessionId = mediationService.createSession(email);
        return ResponseEntity.ok("방이 생성되었습니다. 방 번호: " + sessionId);
    }

    /**
     * 갈등 중재 방 입장
     */
    @PostMapping("/{sessionId}/join")
    public ResponseEntity<String> joinSession(
            Authentication authentication,
            @PathVariable Long sessionId) {
        
        String email = authentication.getName();
        String message = mediationService.joinSession(email, sessionId);
        return ResponseEntity.ok(message);
    }

    /**
     * 라운드별 상황/입장 제출
     */
    @PostMapping("/{sessionId}/{round}")
    public ResponseEntity<String> submitRecord(
            Authentication authentication,
            @PathVariable Long sessionId,
            @PathVariable int round,
            @RequestBody MediationRecordRequest request) {
            
        String email = authentication.getName();
        String resultMessage = mediationService.submitRecord(email, sessionId, round, request);
        return ResponseEntity.ok(resultMessage);
    }

    /**
     * 내 갈등 중재 방 목록 조회 (대기 중, 진행 중, 완료 모두 포함)
     */
    @GetMapping("/session-list")
    public ResponseEntity<List<MediationSessionResponse>> getMyRooms(Authentication authentication) {
        String email = authentication.getName();
        List<MediationSessionResponse> rooms = mediationService.getMySessions(email);
        return ResponseEntity.ok(rooms);
    }
}
