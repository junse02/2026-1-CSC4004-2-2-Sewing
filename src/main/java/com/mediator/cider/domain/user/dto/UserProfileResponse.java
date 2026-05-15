package com.mediator.cider.domain.user.dto;

import com.mediator.cider.domain.attachment.entity.AttachmentType;
import com.mediator.cider.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserProfileResponse {
    private String email;
    private String nickname;
    private String gender;
    private String mbti;
    private String friendCode; // 추가: 내 친구 코드
    private AttachmentType attachmentType; // 애착유형 (설문 전이면 null)
    private String attachmentTypeDescription; // 애착유형 설명 ("불안형" 등)
    private LocalDateTime joinDate; // 가입일

    public static UserProfileResponse of(User user, AttachmentType attachmentType) {
        return UserProfileResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .mbti(user.getMbti())
                .friendCode(user.getFriendCode())
                .attachmentType(attachmentType)
                .attachmentTypeDescription(attachmentType != null ? attachmentType.getDescription() : "검사 전")
                .joinDate(user.getCreatedAt())
                .build();
    }
}
