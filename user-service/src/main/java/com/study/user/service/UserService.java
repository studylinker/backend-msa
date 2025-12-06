package com.study.user.service;

import com.study.user.domain.Role;
import com.study.user.domain.User;
import com.study.user.domain.UserStatus;
import com.study.user.dto.LocationUpdateRequest;
import com.study.user.dto.MannerScoreResponse;
import com.study.user.dto.UserGroupResponse;
import com.study.user.dto.UserRequest;
import com.study.user.dto.UserResponse;
import com.study.user.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ============================
    // 전체 조회 (관리자용)
    // ============================
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ============================
    // 프로필 조회 (일반 사용자)
    // ============================
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        // Lazy 방지 (interestTags 초기화)
        if (user.getInterestTags() != null) {
            user.getInterestTags().size();
        }

        return UserResponse.fromEntity(user);
    }

    // ============================
    // username 조회
    // ============================
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    // ============================
    // 회원가입 (UserResponse 반환)
    // ============================
    @Transactional
    public UserResponse save(UserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(Role.USER); // 기본 USER
        user.setInterestTags(request.getInterestTags());
        user.setLatitude(request.getLatitude());
        user.setLongitude(request.getLongitude());
        user.setStatus(UserStatus.ACTIVE);

        User saved = userRepository.save(user);

        return UserResponse.fromEntity(saved);
    }

    // ============================
    // 사용자 수정 (관리자/일반 공용)
    // ★ 프론트 PUT /api/users/{userId} 한 번에 처리
    // ============================
    @Transactional
    public User update(Long userId, UserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("수정할 사용자를 찾을 수 없습니다. ID: " + userId));

        // username: null/빈값이 아니면 수정
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }

        // 비밀번호: 값이 있을 때만 변경
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // 이름
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        // 이메일 (NOT NULL 제약 있을 수 있으니 null로 덮어쓰지 않도록)
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }

        // 관심사 태그: null이 아닐 때만 교체
        if (request.getInterestTags() != null) {
            user.setInterestTags(request.getInterestTags());
        }

        // 위치
        if (request.getLatitude() != null) {
            user.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            user.setLongitude(request.getLongitude());
        }

        // 역할: 필요하면 사용, 아니면 null이면 유지
        if (request.getRole() != null && !request.getRole().isBlank()) {
            user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        }

        return userRepository.save(user);
    }

    // ============================
    // 비밀번호 변경 (별도 엔드포인트에서 사용할 수 있음)
    // ============================
    @Transactional
    public void updatePassword(Long userId, String newPassword) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("비밀번호를 변경할 사용자를 찾을 수 없습니다. ID: " + userId));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ============================
    // 위치 업데이트
    // ============================
    @Transactional
    public User updateLocation(Long userId, LocationUpdateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("위치를 변경할 사용자를 찾을 수 없습니다. ID: " + userId));

        user.setLatitude(request.getLatitude());
        user.setLongitude(request.getLongitude());
        return userRepository.save(user);
    }

    // ============================
    // 사용자 삭제
    // ============================
    @Transactional
    public void deleteById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("삭제할 사용자를 찾을 수 없습니다. ID: " + userId));

        userRepository.delete(user);
    }

    // ============================
    // 상태 변경 (Admin 전용)
    // ============================
    @Transactional
    public User updateStatus(Long userId, String newStatus) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("상태를 변경할 사용자를 찾을 수 없습니다. ID: " + userId));

        UserStatus status;
        try {
            status = UserStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 상태 값입니다. 허용 값: ACTIVE, INACTIVE");
        }

        user.setStatus(status);

        return userRepository.save(user);
    }

    // ============================
    // 매너 점수 조회
    // ============================
    public MannerScoreResponse getMannerScore(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        return MannerScoreResponse.fromEntity(user);
    }

    // UserService 안에 추가
    public User getByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
    }
}

