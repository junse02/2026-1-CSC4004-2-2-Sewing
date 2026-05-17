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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
     * 라운드별 내용 제출
     */
    @Transactional
    public AiRoundAnalyzeResponse submitRecord(String email, Long sessionId, int round, MediationRecordRequest request) {
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
        
        // 2명 모두 제출한 경우
        if (roundRecords.size() == 2) {
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

            // 종료 조건 체크 (명세서 기준)
            if (session.getEftStage() != null && session.getEftStage() == 3 
                && session.getStageProgress() != null && session.getStageProgress() >= 90) {
                
                // 보고서 생성 호출
                aiServerClient.generateReport(sessionId);
                session.completeMediation();
            } else {
                session.advanceRound();
            }

            return aiResponse;
        }

        return null; // 대기 중
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
