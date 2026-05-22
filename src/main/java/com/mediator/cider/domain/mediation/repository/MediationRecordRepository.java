package com.mediator.cider.domain.mediation.repository;

import com.mediator.cider.domain.mediation.entity.MediationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediationRecordRepository extends JpaRepository<MediationRecord, Long> {
    // 특정 방의 특정 라운드에 작성된 기록들을 가져옵니다.
    List<MediationRecord> findBySessionIdAndRoundNumber(Long sessionId, int roundNumber);
    
    // 특정 유저가 특정 방의 특정 라운드에 작성한 기록이 있는지 확인합니다.
    boolean existsBySessionIdAndRoundNumberAndUserId(Long sessionId, int roundNumber, Long userId);
    
    // 특정 방의 모든 기록을 작성 시간 순으로 가져옵니다.
    List<MediationRecord> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
