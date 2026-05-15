package com.mediator.cider.domain.attachment.service;

import com.mediator.cider.domain.attachment.dto.AttachmentResultResponse;
import com.mediator.cider.domain.attachment.dto.AttachmentSurveyRequest;
import com.mediator.cider.domain.attachment.entity.AttachmentType;
import com.mediator.cider.domain.attachment.entity.UserAttachment;
import com.mediator.cider.domain.attachment.repository.UserAttachmentRepository;
import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentService {

    private final UserAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;

    // ECR-R 문항 분류 (1번부터 시작)
    private static final Set<Integer> ANXIETY_ITEMS = Set.of(
            2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36 // 불안 18문항 (짝수)
    );

    private static final Set<Integer> AVOIDANCE_ITEMS = Set.of(
            1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35 // 회피 18문항 (홀수)
    );

    // ✅ 한국판 ECR-R 기준 정확한 역채점 문항 14개
    // 회피 역채점 (10개): 3, 15, 19, 22, 25, 27, 29, 31, 33, 35
    // 불안 역채점 (4개): 2, 20, 26, 32
    private static final Set<Integer> REVERSE_SCORED_ITEMS = Set.of(
            2, 3, 15, 19, 20, 22, 25, 26, 27, 29, 31, 32, 33, 35
    );

    @Transactional
    public AttachmentResultResponse submitSurvey(String email, AttachmentSurveyRequest request) {
        // 1. 유저 찾기
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 입력값 검증 (36문항인지 확인)
        List<Integer> answers = request.getAnswers();
        if (answers == null || answers.size() != 36) {
            throw new IllegalArgumentException("설문 응답은 정확히 36개여야 합니다.");
        }

        // 3. 점수 계산 (역채점 및 불안/회피 평균 도출)
        double[] scores = calculateScores(answers);
        double anxietyScore = scores[0];
        double avoidanceScore = scores[1];

        // 4. 애착 유형 판별
        AttachmentType calculatedType = calculateAttachmentType(anxietyScore, avoidanceScore);

        // 5. DB 저장 또는 업데이트
        UserAttachment attachment = attachmentRepository.findByUserId(user.getId())
                .orElse(UserAttachment.builder()
                        .user(user)
                        .build());
        
        attachment.updateResult(calculatedType, anxietyScore, avoidanceScore);
        UserAttachment savedAttachment = attachmentRepository.save(attachment);

        return AttachmentResultResponse.from(savedAttachment);
    }

    /**
     * 36개 응답을 분석하여 [불안 평균, 회피 평균] 배열을 반환합니다.
     */
    private double[] calculateScores(List<Integer> answers) {
        double anxietyTotal = 0;
        double avoidanceTotal = 0;
        int anxietyCount = 0;
        int avoidanceCount = 0;

        for (int i = 0; i < answers.size(); i++) {
            int questionNumber = i + 1; // 문항 번호는 1번부터 시작
            int score = answers.get(i);

            // 1~7점 척도 검증
            if (score < 1 || score > 7) {
                throw new IllegalArgumentException(questionNumber + "번 문항의 점수가 1~7점 사이가 아닙니다: " + score);
            }

            // ✅ 역채점 처리 (8에서 빼면 역채점이 됨: 7->1, 1->7)
            if (REVERSE_SCORED_ITEMS.contains(questionNumber)) {
                score = 8 - score;
            }

            // 불안/회피 그룹에 맞게 점수 누적
            if (ANXIETY_ITEMS.contains(questionNumber)) {
                anxietyTotal += score;
                anxietyCount++;
            } else if (AVOIDANCE_ITEMS.contains(questionNumber)) {
                avoidanceTotal += score;
                avoidanceCount++;
            }
        }

        // 소수점 둘째 자리까지 반올림
        double anxietyAvg = Math.round((anxietyTotal / anxietyCount) * 100.0) / 100.0;
        double avoidanceAvg = Math.round((avoidanceTotal / avoidanceCount) * 100.0) / 100.0;

        return new double[]{anxietyAvg, avoidanceAvg};
    }

    /**
     * 애착 유형 점수 계산
     * 불안 기준: 2.61, 회피 기준: 2.33
     */
    private AttachmentType calculateAttachmentType(double anxiety, double avoidance) {
        double anxietyThreshold = 2.61;
        double avoidanceThreshold = 2.33;
        
        if (anxiety <= anxietyThreshold && avoidance <= avoidanceThreshold) {
            return AttachmentType.SECURE; // 안정형
        } else if (anxiety > anxietyThreshold && avoidance <= avoidanceThreshold) {
            return AttachmentType.ANXIOUS; // 불안형
        } else if (anxiety <= anxietyThreshold && avoidance > avoidanceThreshold) {
            return AttachmentType.AVOIDANT; // 거부회피형
        } else {
            return AttachmentType.FEARFUL_AVOIDANT; // 공포회피형
        }
    }
}
