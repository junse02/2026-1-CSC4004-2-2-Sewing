package com.mediator.cider.domain.mediation.entity;

public enum MediationStatus {
    WAITING,     // 방 생성 후 상대방을 기다리는 상태
    IN_PROGRESS, // 두 명 모두 입장하여 진행 중인 상태
    COMPLETED    // 모든 라운드를 마치고 중재가 완료된 상태
}
