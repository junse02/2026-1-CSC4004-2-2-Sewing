package com.mediator.cider.domain.user.entity;


import com.mediator.cider.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 서비스의 회원 정보를 담는 엔티티
 * 작성자: 성준서
 * DB의 'users' 테이블과 매핑
 */

@Entity
@Table(name = "users"   )
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 (보안을 위한 PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id; // 내부 식별자 (PK)

    @Column(nullable = false, unique = true, length = 100)
    private String email; // 로그인 아이디로 사용되는 이메일

    @Column(nullable = false)
    private String password; // 암호화되어 저장될 비밀번호

    @Column(nullable = false, length = 50)
    private String nickname; // 서비스 내 활동 닉네임
    
    @Column(length = 20)
    private String gender; // 성별

    @Column(length = 10)
    private String mbti; // MBTI

    // 기존 데이터와의 충돌(500 에러) 방지를 위해 nullable = false 임시 해제
    @Column(unique = true, length = 8)
    private String friendCode; // 친구 추가용 고유 코드

    private LocalDateTime deletedAt; // 회원 탈퇴 일시

    /**
     * 회원 생성을 위한 빌더 패턴 적용
     */
    @Builder
    public User(String email, String password, String nickname, String gender, String mbti, String friendCode) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.gender = gender;
        this.mbti = mbti;
        this.friendCode = friendCode;
    }

    // 부분 수정을 고려하여 값이 들어왔을 때만 변경되도록 처리
    public void updateProfile(String nickname, String gender, String mbti) {
        if (nickname != null) this.nickname = nickname;
        if (gender != null) this.gender = gender;
        if (mbti != null) this.mbti = mbti;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
