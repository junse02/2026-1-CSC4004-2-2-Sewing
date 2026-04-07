package com.mediator.cider.domain.user.service;

import com.mediator.cider.domain.user.dto.UserJoinRequest;
import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // 암호화 도구 주입

    @Test
    @DisplayName("회원가입 시 비밀번호가 암호화되어 저장되어야 한다")
    void password_encryption_test() {
        // given
        String rawPassword = "myPassword123!";
        UserJoinRequest request = UserJoinRequest.builder()
                .email("encrypt@test.com")
                .password(rawPassword)
                .nickname("보안전문가")
                .build();

        // when
        Long savedId = userService.join(request);
        User savedUser = userRepository.findById(savedId).orElseThrow();

        // then
        // 1. 저장된 비밀번호는 평문(원본)과 달라야 함
        assertThat(savedUser.getPassword()).isNotEqualTo(rawPassword);

        // 2. 암호화된 비밀번호가 BCrypt 형식인지, 원본과 일치하는지 확인
        // passwordEncoder.matches(평문, 암호문)를 사용해야 합니다.
        assertThat(passwordEncoder.matches(rawPassword, savedUser.getPassword())).isTrue();

        System.out.println("암호화된 비밀번호: " + savedUser.getPassword());
    }
}