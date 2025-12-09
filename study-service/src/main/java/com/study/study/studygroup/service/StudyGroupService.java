package com.study.study.studygroup.service;

import com.study.study.groupmember.domain.GroupMember;
import com.study.study.groupmember.dto.GroupMemberResponse;
import com.study.study.groupmember.repository.GroupMemberRepository;
import com.study.study.studyschedule.domain.StudySchedule;
import com.study.study.studyschedule.domain.StudyScheduleStatus;
import com.study.study.studyschedule.dto.StudyScheduleRequest;
import com.study.study.studyschedule.dto.StudyScheduleResponse;
import com.study.study.studyschedule.repository.StudyScheduleRepository;
import com.study.study.studygroup.domain.GroupStatus;
import com.study.study.studygroup.domain.StudyGroup;
import com.study.study.studygroup.dto.NotificationSendRequest;
import com.study.study.studygroup.dto.StudyGroupRequest;
import com.study.study.studygroup.repository.StudyGroupRepository;

// ✅ [추가] user-service 호출용 Client/DTO
import com.study.study.userclient.UserClient;
import com.study.study.userclient.dto.UserSummary;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudyGroupService {

    private final StudyGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final StudyScheduleRepository scheduleRepository;

    // ✅ [추가] user-service 호출용 필드
    private final UserClient userClient;   // 🔹 user-service 호출용

    // 🔹 notification-service 호출용 RestTemplate (기존 그대로 사용)
    private final RestTemplate notificationClient = new RestTemplate();

    private static final String NOTIFICATION_BASE_URL = "http://notification-service:10000";

    public StudyGroupService(
            StudyGroupRepository groupRepository,
            GroupMemberRepository memberRepository,
            StudyScheduleRepository scheduleRepository,
            UserClient userClient              // ✅ [추가] 생성자 주입
    ) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.scheduleRepository = scheduleRepository;
        this.userClient = userClient;       // ✅ [추가] 필드에 할당
    }

    // ===========================
    // 🔔 공통 알림 전송 메서드
    // ===========================
    private void sendNotification(List<Long> userIds, String message, String type) {
        try {
            NotificationSendRequest req = new NotificationSendRequest();
            req.setUserIds(userIds);
            req.setMessage(message);
            req.setType(type);

            notificationClient.postForObject(
                    NOTIFICATION_BASE_URL + "/api/notifications",
                    req,
                    Void.class
            );
        } catch (Exception e) {
            System.out.println("⚠ notification-service 호출 실패: " + e.getMessage());
        }
    }

    // ===========================
    // 🔹 GroupMember → Response + 유저 정보 채우기
    // ===========================
    // ✅ [추가] user-service 를 호출해서 username / name 을 세팅하는 헬퍼 메서드
    private GroupMemberResponse toMemberResponseWithUser(GroupMember member) {
        GroupMemberResponse dto = GroupMemberResponse.fromEntity(member);

        try {
            UserSummary user = userClient.getUserById(member.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setName(user.getName());
            }
        } catch (Exception e) {
            System.out.println("⚠ user-service 호출 실패 userId=" +
                    member.getUserId() + " : " + e.getMessage());
        }

        return dto;
    }

    // ===========================
    // 스터디 그룹 전체 조회
    // ===========================
    public List<StudyGroup> findAll() {
        return groupRepository.findAll();
    }

    // ===========================
    // 단건 조회
    // ===========================
    public StudyGroup findById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("스터디 그룹을 찾을 수 없습니다."));
    }

    // ===========================
    // 그룹 생성 (리더 = 요청자)
    // ===========================
    @Transactional
    public StudyGroup createGroup(StudyGroupRequest request, Long leaderId) {

        StudyGroup group = new StudyGroup();
        group.setLeaderId(leaderId);
        group.setTitle(request.getTitle());
        group.setDescription(request.getDescription());
        group.setMaxMembers(request.getMaxMembers());

        group.setCategory(
                (request.getCategory() == null || request.getCategory().isBlank())
                        ? "[]"
                        : request.getCategory()
        );

        group.setLatitude(request.getLatitude() != null ? BigDecimal.valueOf(request.getLatitude()) : null);
        group.setLongitude(request.getLongitude() != null ? BigDecimal.valueOf(request.getLongitude()) : null);

        StudyGroup saved = groupRepository.save(group);

        // 리더를 멤버로 자동 등록
        GroupMember leaderMember = new GroupMember();
        leaderMember.setGroupId(saved.getGroupId());
        leaderMember.setUserId(leaderId);
        leaderMember.setRole(GroupMember.Role.LEADER);
        leaderMember.setStatus(GroupMember.Status.APPROVED);
        memberRepository.save(leaderMember);

        return saved;
    }

    // ===========================
    // 그룹 수정
    // ===========================
    @Transactional
    public StudyGroup updateGroup(Long groupId,
                                  StudyGroupRequest request,
                                  Long requesterId,
                                  boolean isAdmin) {

        StudyGroup group = findById(groupId);

        if (!isAdmin && !group.getLeaderId().equals(requesterId)) {
            throw new SecurityException("해당 그룹의 리더 또는 관리자만 수정할 수 있습니다.");
        }

        group.setTitle(request.getTitle());
        group.setDescription(request.getDescription());
        group.setMaxMembers(request.getMaxMembers());

        group.setCategory(
                (request.getCategory() == null || request.getCategory().isBlank())
                        ? "[]"
                        : request.getCategory()
        );

        group.setLatitude(request.getLatitude() != null ? BigDecimal.valueOf(request.getLatitude()) : null);
        group.setLongitude(request.getLongitude() != null ? BigDecimal.valueOf(request.getLongitude()) : null);

        return groupRepository.save(group);
    }

    // ===========================
    // 그룹 삭제
    // ===========================
    @Transactional
    public void deleteById(Long groupId, Long requesterId, boolean isAdmin) {

        StudyGroup group = findById(groupId);

        if (!isAdmin && !group.getLeaderId().equals(requesterId)) {
            throw new SecurityException("해당 그룹의 리더 또는 관리자만 삭제할 수 있습니다.");
        }

        groupRepository.delete(group);
    }

    // ===========================
    // 그룹 상태 변경
    // ===========================
    @Transactional
    public void updateStatus(Long groupId, String newStatus, Long requesterId, boolean isAdmin) {

        StudyGroup group = findById(groupId);

        if (!isAdmin && !group.getLeaderId().equals(requesterId)) {
            throw new SecurityException("해당 그룹의 리더 또는 관리자만 상태를 변경할 수 있습니다.");
        }

        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("status 값은 비어 있을 수 없습니다.");
        }

        GroupStatus statusEnum = GroupStatus.valueOf(newStatus.trim().toUpperCase());
        group.setStatus(statusEnum);
    }

    // ===========================
    // ⭐ 멤버 목록 조회 (리더 + 일반 멤버 모두)
    // ===========================
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembersVisible(Long groupId, Long requesterId) {

        StudyGroup group = findById(groupId);

        // 요청자가 그룹 멤버인지 확인
        GroupMember requester = memberRepository
                .findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new SecurityException("그룹 멤버만 조회 가능합니다."));

        List<GroupMember> allMembers = memberRepository.findByGroupId(groupId);

        // 리더면 전체(PENDING 포함) 조회
        if (group.getLeaderId().equals(requesterId)) {
            return allMembers.stream()
                    .map(this::toMemberResponseWithUser)
                    .toList();
        }

        // 일반 멤버는 APPROVED 멤버만 조회
        return allMembers.stream()
                .filter(m -> m.getStatus() == GroupMember.Status.APPROVED)
                .map(this::toMemberResponseWithUser)
                .toList();
    }

    // ===========================
    // 멤버 목록 조회 (리더 전용)
    // ===========================
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembersAsLeader(Long groupId, Long requesterId) {

        StudyGroup group = findById(groupId);

        if (!group.getLeaderId().equals(requesterId)) {
            throw new SecurityException("해당 그룹의 리더만 멤버 목록을 조회할 수 있습니다.");
        }

        return memberRepository.findByGroupId(groupId)
                .stream()
                .map(this::toMemberResponseWithUser)
                .toList();
    }

    // ===========================
    // 특정 멤버 조회
    // ===========================
    public GroupMemberResponse getGroupMember(Long groupId, Long userId) {
        GroupMember member = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));

        return toMemberResponseWithUser(member);
    }

    // ===========================
    // 리더 조회
    // ===========================
    public GroupMemberResponse getGroupLeader(Long groupId) {
        GroupMember leader = memberRepository.findByGroupIdAndRole(groupId, GroupMember.Role.LEADER)
                .orElseThrow(() -> new IllegalArgumentException("리더가 존재하지 않습니다."));

        return toMemberResponseWithUser(leader);
    }

    // ===========================
    // 가입 신청
    // ===========================
    @Transactional
    public GroupMemberResponse requestJoinGroup(Long groupId, Long userId) {

        StudyGroup group = findById(groupId);

        if (memberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new IllegalArgumentException("이미 신청했거나 가입된 유저입니다.");
        }

        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(GroupMember.Role.MEMBER);
        member.setStatus(GroupMember.Status.PENDING);

        GroupMember saved = memberRepository.save(member);

        sendNotification(
                List.of(group.getLeaderId()),
                "새로운 스터디 가입 요청이 도착했습니다.",
                "REQUEST"
        );

        return toMemberResponseWithUser(saved);
    }

    // ===========================
    // 가입 승인
    // ===========================
    @Transactional
    public void approveMember(Long groupId, Long targetUserId, Long leaderId) {

        StudyGroup group = findById(groupId);

        if (!group.getLeaderId().equals(leaderId)) {
            throw new SecurityException("리더만 승인할 수 있습니다.");
        }

        GroupMember member = memberRepository
                .findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));

        member.setStatus(GroupMember.Status.APPROVED);

        sendNotification(
                List.of(targetUserId),
                "스터디 가입 요청이 승인되었습니다.",
                "REQUEST"
        );
    }

    // ===========================
    // 가입 거절
    // ===========================
    @Transactional
    public void rejectMember(Long groupId, Long targetUserId, Long leaderId) {

        StudyGroup group = findById(groupId);

        if (!group.getLeaderId().equals(leaderId)) {
            throw new SecurityException("리더만 거절할 수 있습니다.");
        }

        GroupMember member = memberRepository
                .findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));

        member.setStatus(GroupMember.Status.REJECTED);

        sendNotification(
                List.of(targetUserId),
                "스터디 가입 요청이 거절되었습니다.",
                "REQUEST"
        );
    }

    // ===========================
    // 스케줄 목록 조회
    // ===========================
    public List<StudyScheduleResponse> getGroupSchedules(Long groupId) {
        return scheduleRepository.findByGroupId(groupId)
                .stream()
                .map(StudyScheduleResponse::fromEntity)
                .toList();
    }

    // ===========================
    // 스케줄 생성 (리더만)
    // ===========================
    @Transactional
    public StudyScheduleResponse createSchedule(Long groupId, Long leaderId, StudyScheduleRequest request) {

        StudyGroup group = findById(groupId);

        if (!group.getLeaderId().equals(leaderId)) {
            throw new SecurityException("리더만 일정 등록이 가능합니다.");
        }

        StudySchedule schedule = new StudySchedule();
        schedule.setGroupId(groupId);
        schedule.setUserId(leaderId);
        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setLocation(request.getLocation());
        schedule.setStatus(StudyScheduleStatus.SCHEDULED);

        StudySchedule saved = scheduleRepository.save(schedule);

        List<Long> members = memberRepository.findByGroupId(groupId)
                .stream()
                .map(GroupMember::getUserId)
                .toList();

        sendNotification(
                members,
                "새로운 스터디 일정이 등록되었습니다.",
                "SCHEDULE"
        );

        return StudyScheduleResponse.fromEntity(saved);
    }

    // ===========================
    // 내가 참여(승인)한 스터디 그룹 목록
    // ===========================
    public List<StudyGroup> findJoinedGroups(Long userId) {

        List<GroupMember> members =
                memberRepository.findByUserIdAndStatus(userId, GroupMember.Status.APPROVED);

        List<Long> groupIds = members.stream()
                .map(GroupMember::getGroupId)
                .distinct()
                .toList();

        return groupRepository.findAllById(groupIds);
    }
}