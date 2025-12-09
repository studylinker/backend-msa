package com.study.study.groupmember.service;

import com.study.study.groupmember.domain.GroupMember;
import com.study.study.groupmember.dto.GroupMemberResponse;
import com.study.study.groupmember.repository.GroupMemberRepository;
import com.study.study.studygroup.domain.StudyGroup;
import com.study.study.studygroup.repository.StudyGroupRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class GroupMemberService {

    private final GroupMemberRepository repository;
    private final StudyGroupRepository studyGroupRepository; // 🔥 leader_id 조회용
    private final RestTemplate rt; // user-service 호출용 (필요하면 사용)

    public GroupMemberService(GroupMemberRepository repository,
                              StudyGroupRepository studyGroupRepository,
                              RestTemplate rt) {
        this.repository = repository;
        this.studyGroupRepository = studyGroupRepository;
        this.rt = rt;
    }

    // ================================
    // 🔥 그룹 리더 여부 확인 (Study_groups.leader_id 기반)
    // ================================
    private boolean isLeader(Long groupId, Long requesterId) {

        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "스터디 그룹을 찾을 수 없습니다. groupId=" + groupId));

        Long leaderId = group.getLeaderId();

        if (leaderId == null) {
            System.out.println("[WARN] groupId=" + groupId + " 의 leader_id 가 null입니다.");
            return false;
        }

        return leaderId.equals(requesterId);
    }

    // ================================
    // 🔥 관리자 전용 상태 변경
    // ================================
    @Transactional
    public GroupMemberResponse updateStatusAsAdmin(Long memberId,
                                                   String newStatus,
                                                   boolean isAdmin,
                                                   String authHeader) {

        if (!isAdmin) {
            throw new SecurityException("관리자만 상태를 변경할 수 있습니다.");
        }

        GroupMember member = repository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        member.setStatus(GroupMember.Status.valueOf(newStatus));

        return GroupMemberResponse.fromEntity(member);
    }

    // ================================
    // 🔥 관리자 & 리더 전용 멤버 삭제
    // ================================
    @Transactional
    public void deleteByIdAsAdmin(Long memberId, Long requesterId, boolean isAdmin) {

        GroupMember member = repository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        Long groupId = member.getGroupId();

        // 🔥 DB 기반 리더 체크
        boolean leader = isLeader(groupId, requesterId);

        // 🔐 관리자도 아니고, 리더도 아니면 강퇴 불가
        if (!isAdmin && !leader) {
            throw new SecurityException("리더 또는 관리자만 멤버를 삭제할 수 있습니다.");
        }

        // 🔐 리더가 자기 자신 강퇴 방지
        if (leader && requesterId.equals(member.getUserId())) {
            throw new SecurityException("리더는 자기 자신을 강퇴할 수 없습니다.");
        }

        repository.delete(member);
    }
}