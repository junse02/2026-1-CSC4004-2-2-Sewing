package com.mediator.cider.domain.mediation.controller;

import com.mediator.cider.domain.mediation.dto.CycleDefinitionGetResponse;
import com.mediator.cider.domain.mediation.dto.MediationRecordRequest;
import com.mediator.cider.domain.mediation.dto.MediationRecordResponse;
import com.mediator.cider.domain.mediation.dto.MediationSessionResponse;
import com.mediator.cider.domain.mediation.dto.ai.AiRoundAnalyzeResponse;
import com.mediator.cider.domain.mediation.dto.ai.CycleDefinitionResponse;
import com.mediator.cider.domain.mediation.dto.ai.CycleExploreResponse;
import com.mediator.cider.domain.mediation.dto.ai.CycleRequest;
import com.mediator.cider.domain.mediation.entity.MediationReport;
import com.mediator.cider.domain.mediation.service.MediationService;
import io.swagger.v3.oas.annotations.Operation;
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

    @PostMapping
    public ResponseEntity<String> createSession(Authentication authentication) {
        String email = authentication.getName();
        Long sessionId = mediationService.createSession(email);
        return ResponseEntity.ok("방이 생성되었습니다. 방 번호: " + sessionId);
    }

    @PostMapping("/{sessionId}/join")
    public ResponseEntity<String> joinSession(
            Authentication authentication,
            @PathVariable Long sessionId) {
        
        String email = authentication.getName();
        String message = mediationService.joinSession(email, sessionId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/{sessionId}/{round}")
    public ResponseEntity<?> submitRecord(
            Authentication authentication,
            @PathVariable Long sessionId,
            @PathVariable int round,
            @RequestBody MediationRecordRequest request) {
            
        String email = authentication.getName();
        AiRoundAnalyzeResponse aiResponse = mediationService.submitRecord(email, sessionId, round, request);
        
        if (aiResponse != null) {
            return ResponseEntity.ok(aiResponse);
        } else {
            return ResponseEntity.ok(round + "라운드 제출이 완료되었습니다. 상대방의 제출을 기다리는 중입니다.");
        }
    }

    @GetMapping("/session-list")
    public ResponseEntity<List<MediationSessionResponse>> getMyRooms(Authentication authentication) {
        String email = authentication.getName();
        List<MediationSessionResponse> rooms = mediationService.getMySessions(email);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{sessionId}/current-round")
    public ResponseEntity<Integer> getCurrentRound(@PathVariable Long sessionId) {
        return ResponseEntity.ok(mediationService.getCurrentRound(sessionId));
    }

    @GetMapping("/{sessionId}/records")
    public ResponseEntity<List<MediationRecordResponse>> getRecords(@PathVariable Long sessionId) {
        return ResponseEntity.ok(mediationService.getRecords(sessionId));
    }

    @GetMapping("/{sessionId}/cycle/definition")
    public ResponseEntity<CycleDefinitionGetResponse> getCycleDefinition(@PathVariable Long sessionId) {
        return ResponseEntity.ok(mediationService.getCycleDefinition(sessionId));
    }

    @Operation(summary = "보고서 생성 요청", description = "현재까지의 대화 내용을 바탕으로 AI에게 보고서 생성을 요청합니다. (언제든지 호출 가능)")
    @PostMapping("/{sessionId}/generate-report")
    public ResponseEntity<String> generateReport(@PathVariable Long sessionId) {
        return ResponseEntity.ok(mediationService.generateReport(sessionId));
    }

    @GetMapping("/{sessionId}/report")
    public ResponseEntity<List<MediationReport>> getReport(@PathVariable Long sessionId) {
        return ResponseEntity.ok(mediationService.getReports(sessionId));
    }

    // --- 사이클 API ---

    @PostMapping("/{sessionId}/cycle/explore")
    public ResponseEntity<CycleExploreResponse> exploreCycle(@PathVariable Long sessionId) {
        return ResponseEntity.ok(mediationService.exploreCycle(sessionId));
    }

    @PostMapping("/{sessionId}/cycle/define")
    public ResponseEntity<CycleDefinitionResponse> defineCycle(
            @PathVariable Long sessionId,
            @RequestBody CycleRequest request) {
        return ResponseEntity.ok(mediationService.defineCycle(sessionId, request));
    }
}
