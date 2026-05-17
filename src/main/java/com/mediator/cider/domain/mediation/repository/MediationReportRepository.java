package com.mediator.cider.domain.mediation.repository;

import com.mediator.cider.domain.mediation.entity.MediationReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediationReportRepository extends JpaRepository<MediationReport, Long> {
    List<MediationReport> findBySessionId(Long sessionId);
}
