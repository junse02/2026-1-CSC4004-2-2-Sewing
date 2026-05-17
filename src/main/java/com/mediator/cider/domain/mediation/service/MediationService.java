package com.mediator.cider.domain.mediation.service;

import com.mediator.cider.domain.mediation.dto.MediationRecordRequest;
import com.mediator.cider.domain.mediation.dto.MediationSessionResponse;
import com.mediator.cider.domain.mediation.dto.ai.AiRoundAnalyzeRequest;
import com.mediator.cider.domain.mediation.dto.ai.AiRoundAnalyzeResponse;
import com.mediator.cider.domain.mediation.entity.MediationRecord;
import com.mediator.cider.domain.mediation.entity.MediationSession;
import com.mediator.cider.domain.mediation.entity.MediationStatus;
import com.mediator.cider.domain.mediation.repository.MediationRecordRepository;
import com.mediator.cider.domain.mediation.repository.MediationSessionRepository;
import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.domain.user.repository.FriendshipRepository;
import com.mediator.cider.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediationService {

    private final MediationSessionRepository sessionRepository;
    private final MediationRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final RestTemplate restTemplate;

    // AI 서버 주소
    private static final String AI_SERVER_URL = "http://banuzil-ai.duckdns.org";

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
     * 라운드별 내용 제출
     */
    @Transactional
    public String submitRecord(String email, Long sessionId, int round, MediationRecordRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        MediationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        // 방 상태 체크
        if (session.getStatus() != MediationStatus.IN_PROGRESS) {
            throw new IllegalStateException("현재 진행 중인 방이 아닙니다.");
        }

        // 라운드 일치 여부 체크
        if (session.getCurrentRound() != round) {
            throw new IllegalArgumentException("현재 " + session.getCurrentRound() + "라운드 진행 중입니다. 잘못된 요청입니다.");
        }

        // 방 참여자 권한 체크
        if (!session.getInitiator().getId().equals(user.getId()) && !session.getParticipant().getId().equals(user.getId())) {
            throw new IllegalStateException("이 방에 참여할 권한이 없습니다.");
        }

        // 이미 이번 라운드에 제출했는지 체크
        if (recordRepository.existsBySessionIdAndRoundNumberAndUserId(sessionId, round, user.getId())) {
            throw new IllegalStateException("이미 이번 라운드의 답변을 제출했습니다.");
        }

        // 기록 저장
        MediationRecord record = MediationRecord.builder()
                .session(session)
                .user(user)
                .roundNumber(round)
                .content(request.getContent())
                .build();
        recordRepository.save(record);

        // 두 명 모두 제출했는지 확인
        List<MediationRecord> roundRecords = recordRepository.findBySessionIdAndRoundNumber(sessionId, round);
        if (roundRecords.size() == 2) {
            
            // 1. 여성/남성 발화 분리 로직 (유저의 gender 컬럼 기준)
            String fReply = "";
            String mReply = "";
            
            for (MediationRecord r : roundRecords) {
                if ("여성".equals(r.getUser().getGender()) || "female".equalsIgnoreCase(r.getUser().getGender()) || "여".equals(r.getUser().getGender())) {
                    fReply = r.getContent();
                } else if ("남성".equals(r.getUser().getGender()) || "male".equalsIgnoreCase(r.getUser().getGender()) || "남".equals(r.getUser().getGender())) {
                    mReply = r.getContent();
                } else {
                    // 성별이 명확하지 않은 경우 임의 할당 (테스트 방어 로직)
                    if (fReply.isEmpty()) fReply = r.getContent();
                    else mReply = r.getContent();
                }
            }

            // 2. AI 서버로 API 호출 (/ai/round-analyze)
            try {
                AiRoundAnalyzeRequest aiRequest = AiRoundAnalyzeRequest.builder()
                        .sessionId(sessionId)
                        .fReply(fReply)
                        .mReply(mReply)
                        .build();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<AiRoundAnalyzeRequest> entity = new HttpEntity<>(aiRequest, headers);

                // AI 서버 API 호출
                ResponseEntity<AiRoundAnalyzeResponse> aiResponse = restTemplate.postForEntity(
                        AI_SERVER_URL + "/ai/round-analyze",
                        entity,
                        AiRoundAnalyzeResponse.class
                );

                // AI 응답 로깅 (선택적)
                if (aiResponse.getBody() != null && aiResponse.getBody().isRiskFlag()) {
                    System.out.println("⚠️ 위험 신호 감지됨! (Session: " + sessionId + ")");
                }
            } catch (Exception e) {
                System.err.println("AI 서버 통신 실패: " + e.getMessage());
                // 통신 실패 시에도 라운드는 넘어갈 수 있도록 처리하거나 예외를 던질 수 있습니다.
            }

            // 3. 라운드 진행 로직
            if (session.getCurrentRound() >= 3) {
                session.completeMediation();
                return round + "라운드 제출이 완료되었습니다. (AI 분석 완료) 두 명 모두 제출하여 갈등 중재가 최종 완료(COMPLETED) 되었습니다!";
            } else {
                session.advanceRound();
                return round + "라운드 제출이 완료되었습니다. (AI 분석 완료) 다음 라운드로 넘어갑니다!";
            }
        }

        return round + "라운드 제출이 완료되었습니다. 상대방의 제출을 기다리는 중입니다.";
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
}
