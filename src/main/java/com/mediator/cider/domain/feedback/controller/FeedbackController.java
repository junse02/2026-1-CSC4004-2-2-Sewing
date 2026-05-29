package com.mediator.cider.domain.feedback.controller;

import com.mediator.cider.domain.feedback.dto.FeedbackRequest;
import com.mediator.cider.domain.feedback.service.FeedbackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "feedback-controller", description = "피드백(별점, 후기) API")
@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/{sessionId}")
    public ResponseEntity<String> submitFeedback(
            Authentication authentication,
            @PathVariable Long sessionId,
            @RequestBody FeedbackRequest request) {
        
        String email = authentication.getName();
        feedbackService.saveFeedback(email, sessionId, request);
        return ResponseEntity.ok("피드백이 성공적으로 제출되었습니다.");
    }
}
