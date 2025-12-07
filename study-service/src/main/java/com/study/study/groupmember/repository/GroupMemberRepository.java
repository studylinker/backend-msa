package com.study.study.groupmember.repository;

import com.study.study.groupmember.domain.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    // 🔥 연관관계 제거 후 groupId 필드 기반으로 변경
    List<GroupMember> findByGroupId(Long groupId);

    // 🔥 기존 findByGroupGroupIdAndUserUserId 제거 → ID 기반으로 변경
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    // 🔥 리더 조회도 groupId + role 로 변경
    Optional<GroupMember> findByGroupIdAndRole(Long groupId, GroupMember.Role role);

    // 🔥 이미 가입했는지 확인
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    // 🔥 승인된 멤버만 조회
    List<GroupMember> findByUserIdAndStatus(Long userId, GroupMember.Status status);
}
