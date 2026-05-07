package com.mediator.cider.domain.user.repository;

import com.mediator.cider.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User 엔티티에 대한 DB 접근 처리를 담당하는 저장소
 * 작성자: 성준서
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 활성 상태인 회원을 조회 (탈퇴한 회원 제외)
     * @param email 조회할 이메일
     * @return Optional 객체로 감싸진 User (존재하지 않을 수 있음)
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    /**
     * 해당 이메일이 이미 존재하는 활성 회원인지 확인 (중복 가입 방지용)
     * @param email 확인할 이메일
     * @return 존재 여부 (true/false)
     */
    boolean existsByEmailAndDeletedAtIsNull(String email);
}
