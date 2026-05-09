package com.mediator.cider.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FriendAddRequest {
    private String friendCode; // 친구 추가를 위한 고유 코드
}
