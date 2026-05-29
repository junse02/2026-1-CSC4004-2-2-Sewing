package com.mediator.cider.domain.user.service;

import com.mediator.cider.domain.attachment.entity.UserAttachment;
import com.mediator.cider.domain.attachment.repository.UserAttachmentRepository;
import com.mediator.cider.domain.mediation.entity.MediationSession;
import com.mediator.cider.domain.mediation.entity.MediationStatus;
import com.mediator.cider.domain.mediation.repository.MediationSessionRepository;
import com.mediator.cider.domain.user.dto.*;
import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.domain.user.repository.UserRepository;
import com.mediator.cider.global.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAttachmentRepository userAttachmentRepository;
    private final MediationSessionRepository mediationSessionRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public Long join(UserJoinRequest request) {
        validateDuplicateEmail(request.getEmail());
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String friendCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .gender(request.getGender())
                .mbti(request.getMbti())
                .friendCode(friendCode)
                .build();
                
        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
    }

    public String login(UserLoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return jwtProvider.createToken(user.getEmail());
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        UserAttachment attachment = userAttachmentRepository.findByUserId(user.getId())
                .orElse(null);

        return UserProfileResponse.of(user, attachment != null ? attachment.getType() : null);
    }

    @Transactional
    public UserProfileResponse updateMyProfile(String email, UserProfileUpdateRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.updateProfile(request.getNickname(), request.getGender(), request.getMbti());

        UserAttachment attachment = userAttachmentRepository.findByUserId(user.getId())
                .orElse(null);
                
        return UserProfileResponse.of(user, attachment != null ? attachment.getType() : null);
    }

    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        user.delete();
    }

    @Transactional(readOnly = true)
    public MyPageStatsResponse getMyPageStats(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<MediationSession> sessions = mediationSessionRepository.findAllByUser(user);

        if (sessions.isEmpty()) {
            return MyPageStatsResponse.builder()
                    .totalConflictCount(0)
                    .agreementRate(0.0)
                    .monthlyConflictCounts(List.of())
                    .build();
        }

        long totalConflictCount = sessions.size();

        long completedCount = sessions.stream()
                .filter(s -> s.getStatus() == MediationStatus.COMPLETED)
                .count();

        double agreementRate = (double) completedCount / totalConflictCount * 100.0;
        agreementRate = Math.round(agreementRate * 10.0) / 10.0; // 소수점 첫째 자리까지

        Map<String, Long> monthlyCountsMap = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()
                ));

        List<MonthlyConflictCount> monthlyConflictCounts = monthlyCountsMap.entrySet().stream()
                .map(entry -> new MonthlyConflictCount(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> b.getMonth().compareTo(a.getMonth())) // 최신순 정렬
                .collect(Collectors.toList());

        return MyPageStatsResponse.builder()
                .totalConflictCount(totalConflictCount)
                .agreementRate(agreementRate)
                .monthlyConflictCounts(monthlyConflictCounts)
                .build();
    }
}
