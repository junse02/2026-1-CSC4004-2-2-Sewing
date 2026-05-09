package com.mediator.cider.domain.attachment.controller;

import com.mediator.cider.domain.attachment.dto.AttachmentResultResponse;
import com.mediator.cider.domain.attachment.dto.AttachmentSurveyRequest;
import com.mediator.cider.domain.attachment.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * 애착 유형 설문 제출 및 결과 저장
     */
    @PostMapping("/survey")
    public ResponseEntity<AttachmentResultResponse> submitSurvey(
            Authentication authentication,
            @RequestBody AttachmentSurveyRequest request) {
        
        // 인증 필터를 통과한 유저의 이메일
        String email = authentication.getName();
        
        AttachmentResultResponse response = attachmentService.submitSurvey(email, request);
        return ResponseEntity.ok(response);
    }
}
