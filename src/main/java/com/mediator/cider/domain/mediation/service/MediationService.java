package com.mediator.cider.domain.mediation.service;

import com.mediator.cider.domain.mediation.dto.CycleDefinitionGetResponse;
import com.mediator.cider.domain.mediation.dto.MediationRecordRequest;
import com.mediator.cider.domain.mediation.dto.MediationRecordResponse;
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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediationService {

    private final MediationSessionRepository sessionRepository;
    private final MediationRecordRepository recordRepository;
    private final MediationReportRepository reportRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final AiServerClient aiServerClient;
    private final EntityManager entityManager;
    
    @Autowired
    @Lazy
    private MediationService self; 

    @Transactional
    public Long createSession(String email) {
        User initiator = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        MediationSession session = MediationSession.builder()
                .initiator(initiator)
                .status(MediationStatus.WAITING)
                .currentRound(0)
                .build();

        return sessionRepository.save(session).getId();
    }

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

    public AiRoundAnalyzeResponse submitRecord(String email, Long sessionId, int round, MediationRecordRequest request) {
        boolean isBothSubmitted = self.saveRecordAndCheckBothSubmitted(email, sessionId, round, request);
        if (isBothSubmitted) {
            return self.processAiAnalysis(sessionId, round);
        }
        return null;
    }

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

        AiRoundAnalyzeResponse aiResponse = aiServerClient.roundAnalyze(sessionId, fReply, mReply);

        List<MediationRecord> updatedRecords = recordRepository.findBySessionIdAndRoundNumber(sessionId, round);

        boolean needsCycle = aiResponse.isNeedsCycleDefinition();
        for (MediationRecord r : updatedRecords) {
            r.updateNeedsCycleDefinition(needsCycle);
        }
        
        entityManager.refresh(session);

        if (session.getEftStage() != null && session.getEftStage() == 3 
            && session.getStageProgress() != null && session.getStageProgress() >= 90) {
            
            try {
                aiServerClient.generateReport(sessionId);
            } catch (Exception e) {
                System.err.println("보고서 생성 호출 중 에러 발생: " + e.getMessage());
            }
            session.completeMediation();
        } 
        
        return aiResponse;
    }

    @Transactional(readOnly = true)
    public CycleExploreResponse exploreCycle(Long sessionId) {
        return aiServerClient.cycleExplore(sessionId);
    }

    @Transactional
    public CycleDefinitionResponse defineCycle(Long sessionId, CycleRequest request) {
        // 1. AI 서버에 사이클 정의 요청
        CycleDefinitionResponse response = aiServerClient.cycleDefine(sessionId, request.getFExploreAnswer(), request.getMExploreAnswer());

        // 2. 세션 정보 가져오기
        MediationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        User femaleUser = null;
        User maleUser = null;

        if ("female".equalsIgnoreCase(session.getInitiator().getGender())) {
            femaleUser = session.getInitiator();
            maleUser = session.getParticipant();
        } else {
            femaleUser = session.getParticipant();
            maleUser = session.getInitiator();
        }

        // 명세서에 따라 round_number는 직전 완료 라운드 (현재 라운드 - 1) 사용
        int prevRoundNumber = session.getCurrentRound() > 1 ? session.getCurrentRound() - 1 : 1;

        // 3. 브릿지 메시지가 비어있지 않다면 DB에 INSERT
        // DB 스키마 제약조건(nullable=false)을 피하기 위해 content에 빈 문자열("") 삽입
        if (StringUtils.hasText(response.getFMessage())) {
            MediationRecord bridgeRecord = MediationRecord.builder()
                    .session(session)
                    .user(femaleUser)
                    .roundNumber(prevRoundNumber) 
                    .content("") // 유저 발화 없음 (빈 문자열)
                    .aiResponse(response.getFMessage())
                    .build();
            recordRepository.save(bridgeRecord);
        }
        if (StringUtils.hasText(response.getMMessage())) {
            MediationRecord bridgeRecord = MediationRecord.builder()
                    .session(session)
                    .user(maleUser)
                    .roundNumber(prevRoundNumber) 
                    .content("") // 유저 발화 없음 (빈 문자열)
                    .aiResponse(response.getMMessage())
                    .build();
            recordRepository.save(bridgeRecord);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public CycleDefinitionGetResponse getCycleDefinition(Long sessionId) {
        MediationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));
        return new CycleDefinitionGetResponse(session.getCycleDefinition());
    }

    @Transactional(readOnly = true)
    public List<MediationReport> getReports(Long sessionId) {
        return reportRepository.findBySessionId(sessionId);
    }

    @Transactional(readOnly = true)
    public List<MediationSessionResponse> getMySessions(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<MediationSession> sessions = sessionRepository.findAllByUser(user);

        return sessions.stream()
                .map(MediationSessionResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Integer getCurrentRound(Long sessionId) {
        MediationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));
        return session.getCurrentRound();
    }

    @Transactional(readOnly = true)
    public List<MediationRecordResponse> getRecords(Long sessionId) {
        List<MediationRecord> records = recordRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return records.stream()
                .map(MediationRecordResponse::from)
                .collect(Collectors.toList());
    }
}
