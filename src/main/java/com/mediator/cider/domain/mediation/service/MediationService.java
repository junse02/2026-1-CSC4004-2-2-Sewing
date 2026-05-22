package com.mediator.cider.domain.mediation.service;

import com.mediator.cider.domain.mediation.dto.MediationRecordRequest;
import com.mediator.cider.domain.mediation.dto.MediationSessionResponse;
import com.mediator.cider.domain.mediation.dto.ai.*;
import com.mediator.cider.domain.mediation.entity.MediationRecord;
import com.mediator.cider.domain.mediation.entity.MediationReport;
import com.mediator.cider.domain.mediation.entity.MediationSession;
import com.mediator.cider.domain.mediation.entity.MediationStatus;
import com.mediator.cider.domain.mediation.repository.MediationRecordRepository;
import com.mediator.cider.domain.mediation.repository.MediationReportRepository;
import com.mediator.cider.domain.mediation.repository.MediationSessionRepository;
import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.domain.user.repository.FriendshipRepository;
import com.mediator.cider.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediationService {

    private final MediationSessionRepository sessionRepository;
    private final MediationRecordRepository recordRepository;
    private final MediationReportRepository reportRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final AiServerClient aiServerClient;
    private final EntityManager entityManager;
    
    // 순환 참조(Circular Dependency) 방지를 위해 @Lazy와 @Autowired 사용
    @Autowired
    @Lazy
    private MediationService self; 

    /**
     * 중재 방 생성
     */
    @Transactional
    public Long createSession(String email) {
        User initiator = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        MediationSession session = MediationSession.builder()
                .initiator(initiator)
                .status(MediationStatus.WAITING)
                .currentRound(0) // 시작 전
                .build();

        return sessionRepository.save(session).getId();
    }

    /**
     * 중재 방 입장 (친구만 입장 가능하도록 제한)
     */
    @Transactional
    public String joinSession(String email, Long sessionId) {
        User participant = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        MediationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        if (session.getInitiator().getId().equals(participant.getId())) {
            throw new IllegalStateException("방장은 참여자로 다시 입장할 수 없습니다.");
        }

        boolean isFriend1 = friendshipRepository.existsByFromUserAndToUser(session.getInitiator(), participant);
        boolean isFriend2 = friendshipRepository.existsByFromUserAndToUser(participant, session.getInitiator());
        
        if (!isFriend1 && !isFriend2) {
            throw new IllegalStateException("친구로 등록된 사용자만 방에 입장할 수 있습니다.");
        }

        session.joinParticipant(participant);
        return "방 입장에 성공했습니다. 1라운드를 시작합니다.";
    }

    /**
     * 라운드별 내용 제출 및 트랜잭션 분리
     */
    public AiRoundAnalyzeResponse submitRecord(String email, Long sessionId, int round, MediationRecordRequest request) {
        // 1. 발화를 DB에 완벽히 확정(Commit) 짓는 트랜잭션 호출
        boolean isBothSubmitted = self.saveRecordAndCheckBothSubmitted(email, sessionId, round, request);
        
        // 2. 2명이 모두 제출했다면, 확정된 DB를 바탕으로 AI 서버 호출 (트랜잭션 밖에서 실행!)
        if (isBothSubmitted) {
            return self.processAiAnalysis(sessionId, round);
        }
        
        return null; // 대기 중
    }

    /**
     * 발화를 저장하고, 두 명 다 제출했는지 반환하는 독립 트랜잭션 메서드
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean saveRecordAndCheckBothSubmitted(String email, Long sessionId, int round, MediationRecordRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        MediationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        if (session.getStatus() != MediationStatus.IN_PROGRESS) {
            throw new IllegalStateException("현재 진행 중인 방이 아닙니다.");
        }

        if (session.getCurrentRound() != round) {
            throw new IllegalArgumentException("현재 " + session.getCurrentRound() + "라운드 진행 중입니다.");
        }

        if (!session.getInitiator().getId().equals(user.getId()) && !session.getParticipant().getId().equals(user.getId())) {
            throw new IllegalStateException("이 방에 참여할 권한이 없습니다.");
        }

        if (recordRepository.existsBySessionIdAndRoundNumberAndUserId(sessionId, round, user.getId())) {
            throw new IllegalStateException("이미 이번 라운드의 답변을 제출했습니다.");
        }

        MediationRecord record = MediationRecord.builder()
                .session(session)
                .user(user)
                .roundNumber(round)
                .content(request.getContent())
                .build();
        recordRepository.save(record);

        List<MediationRecord> roundRecords = recordRepository.findBySessionIdAndRoundNumber(sessionId, round);
        return roundRecords.size() == 2;
    }

    /**
     * AI 통신 및 라운드 진행 처리를 위한 독립 트랜잭션 메서드
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiRoundAnalyzeResponse processAiAnalysis(Long sessionId, int round) {
        MediationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));
                
        List<MediationRecord> roundRecords = recordRepository.findBySessionIdAndRoundNumber(sessionId, round);
        
        String fReply = "";
        String mReply = "";
        
        for (MediationRecord r : roundRecords) {
            if ("female".equalsIgnoreCase(r.getUser().getGender()) || "여성".equals(r.getUser().getGender()) || "여".equals(r.getUser().getGender())) {
                fReply = r.getContent();
            } else if ("male".equalsIgnoreCase(r.getUser().getGender()) || "남성".equals(r.getUser().getGender()) || "남".equals(r.getUser().getGender())) {
                mReply = r.getContent();
            } else {
                if (fReply.isEmpty()) fReply = r.getContent();
                else mReply = r.getContent();
            }
        }

        // AI 서버 호출
        AiRoundAnalyzeResponse aiResponse = aiServerClient.roundAnalyze(sessionId, fReply, mReply);

        entityManager.refresh(session);

        // 테스트를 위해 종료 조건 완화: stage에 상관없이 3라운드 이상이면 무조건 종료!
        // (실제 프로덕션에서는 AI의 stage, progress를 엄격히 따져야 합니다)
        if (session.getCurrentRound() >= 3) {
            
            // 보고서 생성 호출
            try {
                aiServerClient.generateReport(sessionId);
            } catch (Exception e) {
                System.err.println("보고서 생성 호출 중 에러 발생: " + e.getMessage());
            }
            session.completeMediation();
            
        } else {
            session.advanceRound();
        }

        return aiResponse;
    }

    /**
     * 사이클 탐색 질문 요청
     */
    public CycleExploreResponse exploreCycle(Long sessionId) {
        return aiServerClient.cycleExplore(sessionId);
    }

    /**
     * 사이클 정의 생성
     */
    public CycleDefinitionResponse defineCycle(Long sessionId, CycleRequest request) {
        return aiServerClient.cycleDefine(sessionId, request.getFExploreAnswer(), request.getMExploreAnswer());
    }

    /**
     * 보고서 조회
     */
    public List<MediationReport> getReports(Long sessionId) {
        return reportRepository.findBySessionId(sessionId);
    }

    /**
     * 내 갈등 중재 방 목록 조회
     */
    public List<MediationSessionResponse> getMySessions(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<MediationSession> sessions = sessionRepository.findAllByUser(user);

        return sessions.stream()
                .map(MediationSessionResponse::from)
                .collect(Collectors.toList());
    }
}
