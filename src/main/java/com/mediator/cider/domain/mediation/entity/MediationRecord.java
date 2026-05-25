package com.mediator.cider.domain.mediation.entity;

import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

/**
 * 각 라운드별 사용자의 입력 기록을 저장하는 엔티티
 */
@Entity
@Table(name = "mediation_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MediationRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    // 어느 중재 방의 기록인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private MediationSession session;

    // 누가 작성했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 몇 라운드의 기록인지
    @Column(nullable = false)
    private int roundNumber;

    // 작성한 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // AI 서버 응답 추가
    @Column(columnDefinition = "TEXT")
    private String aiResponse;

    // 사이클 정의 필요 여부 추가
    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean needsCycleDefinition = false;

    public void updateNeedsCycleDefinition(boolean needsCycleDefinition) {
        this.needsCycleDefinition = needsCycleDefinition;
    }
}
