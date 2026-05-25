package com.mediator.cider.domain.mediation.service;

import com.mediator.cider.domain.mediation.dto.ai.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Service
public class AiServerClient {
    private final RestClient restClient;

    public AiServerClient(@Value("${ai.server.url:http://banuzil-ai.duckdns.org}") String aiServerUrl) {
        // AI 응답이 매우 느릴 수 있으므로 타임아웃을 60초로 넉넉하게 늘림
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(60));

        this.restClient = RestClient.builder()
                .baseUrl(aiServerUrl)
                .requestFactory(requestFactory)
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
