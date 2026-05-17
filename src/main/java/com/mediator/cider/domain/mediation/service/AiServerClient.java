package com.mediator.cider.domain.mediation.service;

import com.mediator.cider.domain.mediation.dto.ai.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiServerClient {
    private final RestClient restClient;

    public AiServerClient(@Value("${ai.server.url:http://banuzil-ai.duckdns.org}") String aiServerUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(aiServerUrl)
            .build();
    }

    public AiRoundAnalyzeResponse roundAnalyze(Long sessionId, String fReply, String mReply) {
        return restClient.post()
            .uri("/ai/round-analyze")
            .body(AiRoundAnalyzeRequest.builder()
                    .sessionId(sessionId)
                    .fReply(fReply)
                    .mReply(mReply)
                    .build())
            .retrieve()
            .body(AiRoundAnalyzeResponse.class);
    }

    public CycleExploreResponse cycleExplore(Long sessionId) {
        return restClient.post()
            .uri("/ai/cycle")
            .body(CycleRequest.builder()
                    .sessionId(sessionId)
                    .fExploreAnswer("")
                    .mExploreAnswer("")
                    .build())
            .retrieve()
            .body(CycleExploreResponse.class);
    }

    public CycleDefinitionResponse cycleDefine(Long sessionId, String fAnswer, String mAnswer) {
        return restClient.post()
            .uri("/ai/cycle")
            .body(CycleRequest.builder()
                    .sessionId(sessionId)
                    .fExploreAnswer(fAnswer)
                    .mExploreAnswer(mAnswer)
                    .build())
            .retrieve()
            .body(CycleDefinitionResponse.class);
    }

    public ReportResponse generateReport(Long sessionId) {
        return restClient.post()
            .uri("/ai/report")
            .body(ReportRequest.builder().sessionId(sessionId).build())
            .retrieve()
            .body(ReportResponse.class);
    }
}
