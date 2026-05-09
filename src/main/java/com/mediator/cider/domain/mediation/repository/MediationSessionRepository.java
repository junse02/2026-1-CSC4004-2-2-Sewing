package com.mediator.cider.domain.mediation.repository;

import com.mediator.cider.domain.mediation.entity.MediationSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediationSessionRepository extends JpaRepository<MediationSession, Long> {
}
