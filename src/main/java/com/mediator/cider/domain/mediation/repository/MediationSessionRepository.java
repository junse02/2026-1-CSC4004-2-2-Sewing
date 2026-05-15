package com.mediator.cider.domain.mediation.repository;

import com.mediator.cider.domain.mediation.entity.MediationSession;
import com.mediator.cider.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MediationSessionRepository extends JpaRepository<MediationSession, Long> {
    
    // 내가 방장(initiator)이거나 참여자(participant)인 모든 방을 최신순으로 조회
    @Query("SELECT s FROM MediationSession s WHERE s.initiator = :user OR s.participant = :user ORDER BY s.updatedAt DESC")
    List<MediationSession> findAllByUser(@Param("user") User user);
}
