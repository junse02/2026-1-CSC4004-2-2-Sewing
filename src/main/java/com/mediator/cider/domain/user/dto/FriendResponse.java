package com.mediator.cider.domain.user.dto;

import com.mediator.cider.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendResponse {
    private Long friendId;
    private String nickname;
    private String mbti;
    private String attachmentTypeDescription; // 조인해서 가져올 애착유형
    
    public static FriendResponse from(User friendUser, String attachmentTypeDesc) {
        return FriendResponse.builder()
                .friendId(friendUser.getId())
                .nickname(friendUser.getNickname())
                .mbti(friendUser.getMbti())
                .attachmentTypeDescription(attachmentTypeDesc)
                .build();
    }
}
