package com.mediator.cider.domain.user.dto;

import com.mediator.cider.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 데이터를 담는 DTO
 * 작성자: 성준서
 */

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJoinRequest {

    private String email;    // 사용자 식별용 이메일
    private String password; // 비밀번호 (암호화 예정)
    private String nickname; // 사용자 이름
    private String gender;   // 성별
    private String mbti;     // MBTI

    /**
     * DTO를 엔티티 객체로 변환
     * @param encodedPassword 암호화된 비밀번호
     * @return User 엔티티 객체
     */
    public User toEntity(String encodedPassword) {
        return User.builder()
                .email(this.email)
                .password(encodedPassword)
                .nickname(this.nickname)
                .gender(this.gender)
                .mbti(this.mbti)
                .build();
    }
}
