package com.mediator.cider.domain.feedback.service;

import com.mediator.cider.domain.feedback.dto.FeedbackRequest;
import com.mediator.cider.domain.feedback.entity.Feedback;
import com.mediator.cider.domain.feedback.repository.FeedbackRepository;
import com.mediator.cider.domain.mediation.entity.MediationSession;
import com.mediator.cider.domain.mediation.repository.MediationSessionRepository;
import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final MediationSessionRepository mediationSessionRepository;

    @Transactional
    public void saveFeedback(String email, Long sessionId, FeedbackRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        MediationSession session = mediationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        // 한 세션에 대해 한 명의 유저는 한 번만 피드백을 남길 수 있도록 제한
        if (feedbackRepository.existsBySessionIdAndUserId(sessionId, user.getId())) {
            throw new IllegalStateException("이미 이 세션에 대한 피드백을 제출했습니다.");
        }

        Feedback feedback = Feedback.builder()
                .session(session)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        feedbackRepository.save(feedback);
    }
}
