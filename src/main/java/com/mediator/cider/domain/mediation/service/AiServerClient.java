package com.mediator.cider.domain.mediation.service;

import com.mediator.cider.domain.mediation.dto.ai.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * AI 서버(Python FastAPI)와의 HTTP 통신 클라이언트
 * 작성자: 황병부
 */
@Slf4j
@Service
public class AiServerClient {

    private final RestClient restClient;

    public AiServerClient(@Value("${ai.server.url}") String aiServerUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(aiServerUrl)
                .build();
    }

    /**
     * 라운드 분석 요청
     */
    public AiRoundAnalyzeResponse roundAnalyze(Long sessionId, String fReply, String mReply) {
        log.info("[AI] 라운드 분석 요청 - sessionId: {}", sessionId);

        return restClient.post()
                .uri("/ai/round-analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AiRoundAnalyzeRequest(sessionId, fReply, mReply))
                .retrieve()
                .body(AiRoundAnalyzeResponse.class);
    }

    /**
     * 사이클 탐색 질문 요청
     */
    public AiCycleExploreResponse cycleExplore(Long sessionId) {
        log.info("[AI] 사이클 탐색 요청 - sessionId: {}", sessionId);

        return restClient.post()
                .uri("/ai/cycle")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AiCycleRequest(sessionId, "", ""))
                .retrieve()
                .body(AiCycleExploreResponse.class);
    }

    /**
     * 사이클 정의 생성 요청
     */
    public AiCycleDefinitionResponse cycleDefine(Long sessionId, String fAnswer, String mAnswer) {
        log.info("[AI] 사이클 정의 요청 - sessionId: {}", sessionId);

        return restClient.post()
                .uri("/ai/cycle")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AiCycleRequest(sessionId, fAnswer, mAnswer))
                .retrieve()
                .body(AiCycleDefinitionResponse.class);
    }

    /**
     * 최종 보고서 생성 요청
     */
    public AiReportResponse generateReport(Long sessionId) {
        log.info("[AI] 보고서 생성 요청 - sessionId: {}", sessionId);

        return restClient.post()
                .uri("/ai/report")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AiReportRequest(sessionId))
                .retrieve()
                .body(AiReportResponse.class);
    }
}
