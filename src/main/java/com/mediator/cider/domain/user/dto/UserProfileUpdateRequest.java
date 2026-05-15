package com.mediator.cider.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileUpdateRequest {
    private String nickname;
    private String gender;
    private String mbti;
}
