package com.mediator.cider.domain.mediation.service;

import com.mediator.cider.domain.mediation.dto.*;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 갈등 중재 핵심 서비스
 * 작성자: 황병부
 */
@Slf4j
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

        // 방장이 본인 방에 다시 입장하려고 하는 경우 방지
        if (session.getInitiator().getId().equals(participant.getId())) {
            throw new IllegalStateException("방장은 참여자로 다시 입장할 수 없습니다.");
        }

        // 친구 관계인지 확인 (방장과 참여자가 서로 친구인지)
        boolean isFriend1 = friendshipRepository.existsByFromUserAndToUser(session.getInitiator(), participant);
        boolean isFriend2 = friendshipRepository.existsByFromUserAndToUser(participant, session.getInitiator());

        if (!isFriend1 && !isFriend2) {
            throw new IllegalStateException("친구로 등록된 사용자만 방에 입장할 수 있습니다.");
        }

        session.joinParticipant(participant);
        return "방 입장에 성공했습니다. 1라운드를 시작합니다.";
    }

    /**
     * 라운드별 내용 제출 + AI 분석 연동
     */
    @Transactional
    public MediationRoundResponse submitRecord(String email, Long sessionId, int round, MediationRecordRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        MediationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        if (session.getStatus() != MediationStatus.IN_PROGRESS) {
            throw new IllegalStateException("현재 진행 중인 방이 아닙니다.");
        }

        if (session.getCurrentRound() != round) {
            throw new IllegalArgumentException("현재 " + session.getCurrentRound() + "라운드 진행 중입니다. 잘못된 요청입니다.");
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

        // 아직 상대방이 제출하지 않은 경우
        if (roundRecords.size() < 2) {
            return MediationRoundResponse.builder()
                    .message(round + "라운드 제출이 완료되었습니다. 상대방의 제출을 기다리는 중입니다.")
                    .completed(false)
                    .build();
        }

        // 두 명 모두 제출 → AI 서버에 라운드 분석 요청
        String fReply = resolveReplyByRole(session, roundRecords, true);
        String mReply = resolveReplyByRole(session, roundRecords, false);

        AiRoundAnalyzeResponse aiResponse = aiServerClient.roundAnalyze(sessionId, fReply, mReply);
        log.info("[AI] 라운드 분석 완료 - sessionId: {}, needsCycle: {}, risk: {}",
                sessionId, aiResponse.isNeedsCycleDefinition(), aiResponse.isRiskFlag());

        // AI 응답을 각 사용자의 record에 저장
        saveAiResponses(session, roundRecords, aiResponse);

        // 다음 라운드로 진행 (AI 서버가 EFT 스테이지/라운드를 DB에서 직접 관리)
        session.advanceRound();

        // 현재 사용자에게 맞는 AI 메시지 반환
        String myAiMessage = resolveMyAiMessage(session, user, aiResponse);

        return MediationRoundResponse.builder()
                .message(round + "라운드 제출이 완료되었습니다. " + session.getCurrentRound() + "라운드로 넘어갑니다!")
                .aiMessage(myAiMessage)
                .needsCycleExplore(aiResponse.isNeedsCycleDefinition())
                .riskFlag(aiResponse.isRiskFlag())
                .completed(false)
                .build();
    }

    /**
     * 사이클 탐색 질문 요청
     */
    @Transactional
    public CycleExploreResponse getCycleExploreQuestion(String email, Long sessionId) {
        User user = findUserAndValidateParticipant(email, sessionId);
        MediationSession session = sessionRepository.findById(sessionId).orElseThrow();

        AiCycleExploreResponse aiResponse = aiServerClient.cycleExplore(sessionId);

        String myQuestion = isInitiator(session, user) ? aiResponse.getFQuestion() : aiResponse.getMQuestion();

        return CycleExploreResponse.builder()
                .sessionId(sessionId)
                .question(myQuestion)
                .build();
    }

    /**
     * 사이클 정의 (탐색 답변 제출)
     */
    @Transactional
    public CycleDefinitionResponse submitCycleAnswer(String email, Long sessionId, CycleDefineRequest request) {
        User user = findUserAndValidateParticipant(email, sessionId);
        MediationSession session = sessionRepository.findById(sessionId).orElseThrow();

        String fAnswer = isInitiator(session, user) ? request.getAnswer() : "";
        String mAnswer = isInitiator(session, user) ? "" : request.getAnswer();

        AiCycleDefinitionResponse aiResponse = aiServerClient.cycleDefine(sessionId, fAnswer, mAnswer);

        return CycleDefinitionResponse.builder()
                .sessionId(sessionId)
                .cycleDefinition(aiResponse.getCycleDefinition())
                .build();
    }

    /**
     * 최종 보고서 생성 요청
     */
    @Transactional
    public MediationReportResponse generateReport(String email, Long sessionId) {
        User user = findUserAndValidateParticipant(email, sessionId);
        MediationSession session = sessionRepository.findById(sessionId).orElseThrow();

        session.completeMediation();

        AiReportResponse aiResponse = aiServerClient.generateReport(sessionId);

        // 양쪽 보고서 모두 저장
        saveReport(session, session.getInitiator(), aiResponse.getFReport());
        saveReport(session, session.getParticipant(), aiResponse.getMReport());

        // 요청한 사용자의 보고서 반환
        AiReportResponse.ReportSections myReport = isInitiator(session, user)
                ? aiResponse.getFReport() : aiResponse.getMReport();

        return MediationReportResponse.builder()
                .sessionId(sessionId)
                .emotionSummary(myReport.getEmotionSummary())
                .partnerUnderstanding(myReport.getPartnerUnderstanding())
                .mediationPlans(myReport.getMediationPlans())
                .recommendedDialogues(myReport.getRecommendedDialogues())
                .build();
    }

    /**
     * 보고서 조회
     */
    public MediationReportResponse getReport(String email, Long sessionId) {
        User user = findUserAndValidateParticipant(email, sessionId);

        MediationReport report = reportRepository.findBySessionIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("보고서가 아직 생성되지 않았습니다."));

        return MediationReportResponse.from(report);
    }

    /**
     * 내가 참여 중이거나 완료된 모든 방 목록 조회
     */
    public List<MediationSessionResponse> getMySessions(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<MediationSession> sessions = sessionRepository.findAllByUser(user);

        return sessions.stream()
                .map(MediationSessionResponse::from)
                .collect(Collectors.toList());
    }

    // === private helpers ===

    private User findUserAndValidateParticipant(String email, Long sessionId) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        MediationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        if (!session.getInitiator().getId().equals(user.getId())
                && !session.getParticipant().getId().equals(user.getId())) {
            throw new IllegalStateException("이 방에 참여할 권한이 없습니다.");
        }

        return user;
    }

    private boolean isInitiator(MediationSession session, User user) {
        return session.getInitiator().getId().equals(user.getId());
    }

    /**
     * 성별 기반으로 f/m 역할 매핑하여 발화 반환
     * initiator의 gender가 "F"이면 initiator=f, participant=m
     * 그 외(기본): initiator=f, participant=m (fallback)
     */
    private String resolveReplyByRole(MediationSession session, List<MediationRecord> records, boolean isFRole) {
        User initiator = session.getInitiator();
        User participant = session.getParticipant();

        boolean initiatorIsF = "F".equalsIgnoreCase(initiator.getGender());

        User fUser = initiatorIsF ? initiator : participant;
        User mUser = initiatorIsF ? participant : initiator;
        User targetUser = isFRole ? fUser : mUser;

        return records.stream()
                .filter(r -> r.getUser().getId().equals(targetUser.getId()))
                .findFirst()
                .map(MediationRecord::getContent)
                .orElse("");
    }

    private String resolveMyAiMessage(MediationSession session, User user, AiRoundAnalyzeResponse aiResponse) {
        User initiator = session.getInitiator();
        boolean initiatorIsF = "F".equalsIgnoreCase(initiator.getGender());
        boolean userIsF = (initiatorIsF && user.getId().equals(initiator.getId()))
                || (!initiatorIsF && user.getId().equals(session.getParticipant().getId()));

        return userIsF ? aiResponse.getFMessage() : aiResponse.getMMessage();
    }

    private void saveAiResponses(MediationSession session, List<MediationRecord> records, AiRoundAnalyzeResponse aiResponse) {
        User initiator = session.getInitiator();
        boolean initiatorIsF = "F".equalsIgnoreCase(initiator.getGender());

        for (MediationRecord record : records) {
            boolean isInitiatorRecord = record.getUser().getId().equals(initiator.getId());
            boolean isFRecord = (initiatorIsF && isInitiatorRecord) || (!initiatorIsF && !isInitiatorRecord);
            String aiMsg = isFRecord ? aiResponse.getFMessage() : aiResponse.getMMessage();
            record.updateAiResponse(aiMsg);
        }
    }

    private void saveReport(MediationSession session, User user, AiReportResponse.ReportSections sections) {
        MediationReport report = MediationReport.builder()
                .session(session)
                .user(user)
                .emotionSummary(sections.getEmotionSummary())
                .partnerUnderstanding(sections.getPartnerUnderstanding())
                .mediationPlans(sections.getMediationPlans())
                .recommendedDialogues(sections.getRecommendedDialogues())
                .build();
        reportRepository.save(report);
    }
}
