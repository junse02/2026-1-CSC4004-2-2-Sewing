package com.mediator.cider.domain.user.entity;


import com.mediator.cider.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 서비스의 회원 정보를 담는 엔티티
 * 작성자: 성준서
 * DB의 'users' 테이블과 매핑
 */

@Entity
@Table(name = "users"   )
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 (보안을 위한 PROTECTED)
@AllArgsConstructor
@Builder
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

    /**
     * 회원 생성을 위한 빌더 패턴 적용
     */
    @Builder
    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    // 회원 정보 수정 등이 필요할 때 여기에 메서드를 추가
    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }
}
