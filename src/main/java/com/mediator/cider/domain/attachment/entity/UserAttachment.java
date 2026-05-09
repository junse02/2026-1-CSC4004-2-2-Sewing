package com.mediator.cider.domain.attachment.entity;

import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자의 애착 유형 설문 결과를 저장하는 엔티티
 */
@Entity
@Table(name = "user_attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserAttachment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long id;

    // User 엔티티와 1:1 관계 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttachmentType type; // 계산된 최종 애착 유형

    private double anxietyScore;    // 불안 점수
    private double avoidanceScore;  // 회피 점수

    /**
     * 설문 결과 업데이트 편의 메서드
     */
    public void updateResult(AttachmentType type, double anxietyScore, double avoidanceScore) {
        this.type = type;
        this.anxietyScore = anxietyScore;
        this.avoidanceScore = avoidanceScore;
    }
}
