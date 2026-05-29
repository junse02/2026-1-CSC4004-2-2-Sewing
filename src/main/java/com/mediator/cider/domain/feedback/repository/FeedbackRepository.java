package com.mediator.cider.domain.feedback.repository;

import com.mediator.cider.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);
}
