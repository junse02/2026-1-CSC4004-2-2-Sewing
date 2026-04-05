package com.mediator.cider.domain.user.service;

import com.mediator.cider.domain.user.dto.UserJoinRequest;
import com.mediator.cider.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 테스트 후 데이터를 롤백하여 DB를 깨끗하게 유지함
class UserServiceTest {

    @Autowired UserService userService;
    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void join_success() {
        // given (준비)
        UserJoinRequest request = UserJoinRequest.builder()
                .email("test@example.com")
                .password("password123")
                .nickname("테스터")
                .build();

        // when (실행)
        Long savedId = userService.join(request);

        // then (검증)
        assertThat(userRepository.findById(savedId)).isPresent();
        assertThat(userRepository.findById(savedId).get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("중복 이메일 가입 실패 테스트")
    void join_duplicate_email_fail() {
        // given (동일한 이메일로 이미 한 명이 가입됨)
        UserJoinRequest request1 = UserJoinRequest.builder()
                .email("duplicate@example.com")
                .password("pw1")
                .nickname("유저1")
                .build();
        userService.join(request1);

        UserJoinRequest request2 = UserJoinRequest.builder()
                .email("duplicate@example.com")
                .password("pw2")
                .nickname("유저2")
                .build();

        // when & then (실행 시 예외가 발생하는지 확인)
        assertThrows(IllegalStateException.class, () -> {
            userService.join(request2);
        });
    }
}