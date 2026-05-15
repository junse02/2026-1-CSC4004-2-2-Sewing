package com.mediator.cider.domain.user.repository;

import com.mediator.cider.domain.user.entity.Friendship;
import com.mediator.cider.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    // 두 유저가 이미 친구인지 확인 (from -> to 방향)
    boolean existsByFromUserAndToUser(User fromUser, User toUser);

    // 내가 추가한 친구 목록 조회 (fetch join으로 유저 정보도 함께 가져옴)
    @Query("SELECT f FROM Friendship f JOIN FETCH f.toUser WHERE f.fromUser = :user")
    List<Friendship> findAllByFromUserWithToUser(@Param("user") User user);

    // 양방향 친구 삭제 (나 -> 상대방, 상대방 -> 나 모두 삭제)
    @Modifying
    @Query("DELETE FROM Friendship f WHERE (f.fromUser = :user AND f.toUser = :friend) OR (f.fromUser = :friend AND f.toUser = :user)")
    void deleteByUsers(@Param("user") User user, @Param("friend") User friend);
}
