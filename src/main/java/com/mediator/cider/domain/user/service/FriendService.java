package com.mediator.cider.domain.user.service;

import com.mediator.cider.domain.attachment.entity.UserAttachment;
import com.mediator.cider.domain.attachment.repository.UserAttachmentRepository;
import com.mediator.cider.domain.user.dto.FriendResponse;
import com.mediator.cider.domain.user.entity.Friendship;
import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.domain.user.repository.FriendshipRepository;
import com.mediator.cider.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserAttachmentRepository userAttachmentRepository;

    /**
     * 친구 코드로 친구 추가 (쌍방향 친구 맺기)
     */
    @Transactional
    public String addFriend(String myEmail, String friendCode) {
        User me = userRepository.findByEmailAndDeletedAtIsNull(myEmail)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        User friend = userRepository.findByFriendCodeAndDeletedAtIsNull(friendCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 친구 코드입니다."));

        if (me.getId().equals(friend.getId())) {
            throw new IllegalArgumentException("자기 자신은 친구로 추가할 수 없습니다.");
        }

        // 이미 친구인지 확인
        if (friendshipRepository.existsByFromUserAndToUser(me, friend)) {
            throw new IllegalStateException("이미 친구로 등록된 사용자입니다.");
        }

        // 1. 내가 상대방을 친구로 추가
        Friendship myFriendship = Friendship.builder()
                .fromUser(me)
                .toUser(friend)
                .build();
        friendshipRepository.save(myFriendship);

        // 2. (옵션) 상대방도 나를 친구로 추가되도록 쌍방향 처리
        Friendship targetFriendship = Friendship.builder()
                .fromUser(friend)
                .toUser(me)
                .build();
        friendshipRepository.save(targetFriendship);

        return friend.getNickname() + "님과 친구가 되었습니다!";
    }

    /**
     * 내 친구 목록 조회
     */
    public List<FriendResponse> getMyFriends(String myEmail) {
        User me = userRepository.findByEmailAndDeletedAtIsNull(myEmail)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<Friendship> friendships = friendshipRepository.findAllByFromUserWithToUser(me);

        return friendships.stream().map(friendship -> {
            User friend = friendship.getToUser();
            
            // 친구의 애착유형 조회 (설문 안했으면 "검사 전")
            UserAttachment attachment = userAttachmentRepository.findByUserId(friend.getId()).orElse(null);
            String typeDesc = (attachment != null) ? attachment.getType().getDescription() : "검사 전";

            return FriendResponse.from(friend, typeDesc);
        }).collect(Collectors.toList());
    }

    /**
     * 친구 삭제 (쌍방향 삭제)
     */
    @Transactional
    public String deleteFriend(String myEmail, Long friendId) {
        User me = userRepository.findByEmailAndDeletedAtIsNull(myEmail)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 친구 ID입니다."));

        // 친구 관계가 맞는지 확인
        if (!friendshipRepository.existsByFromUserAndToUser(me, friend)) {
            throw new IllegalStateException("친구 관계가 아닙니다.");
        }

        friendshipRepository.deleteByUsers(me, friend);
        return friend.getNickname() + "님을 친구 목록에서 삭제했습니다.";
    }
}
