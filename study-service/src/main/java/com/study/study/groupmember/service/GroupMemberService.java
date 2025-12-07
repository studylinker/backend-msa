package com.study.study.groupmember.service;

import com.study.study.groupmember.domain.GroupMember;
import com.study.study.groupmember.dto.GroupMemberResponse;
import com.study.study.groupmember.repository.GroupMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class GroupMemberService {

    private final GroupMemberRepository repository;
    private final RestTemplate rt = new RestTemplate();

    public GroupMemberService(GroupMemberRepository repository) {
        this.repository = repository;
    }

    // ================================
    // 🔥 그룹 리더 여부 확인 (group-service 호출)
    // ================================
    private boolean isLeader(Long groupId, Long requesterId) {

        // group-service 엔드포인트 예시:
        // GET /api/study-groups/{groupId}/leader
        String url = "http://study-group-service:10002/api/study-groups/" + groupId + "/leader";

        try {
            LeaderDTO leader = rt.getForObject(url, LeaderDTO.class);
            return leader != null && leader.getUserId().equals(requesterId);
        } catch (Exception e) {
            throw new IllegalArgumentException("그룹 리더 정보를 조회할 수 없습니다.");
        }
    }

    // ================================
    // 🔥 user-service API
    // ================================
    private UserDTO getUser(Long userId) {
        String url = "http://user-service:10001/api/users/" + userId;
        return rt.getForObject(url, UserDTO.class);
    }

    // ================================
    // 🔥 관리자 전용 상태 변경
    // ================================
    @Transactional
    public GroupMemberResponse updateStatusAsAdmin(Long memberId,
                                                   String newStatus,
                                                   boolean isAdmin) {

        if (!isAdmin) {
            throw new SecurityException("관리자만 상태를 변경할 수 있습니다.");
        }

        GroupMember member = repository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        member.setStatus(GroupMember.Status.valueOf(newStatus));

        // 응답 생성
        GroupMemberResponse res = GroupMemberResponse.fromEntity(member);

        UserDTO user = getUser(member.getUserId());
        res.setUsername(user.getUsername());
        res.setName(user.getName());

        return res;
    }

    // ================================
    // 🔥 관리자 전용 멤버 삭제
    // ================================
    @Transactional
    public void deleteByIdAsAdmin(Long memberId, boolean isAdmin) {

        if (!isAdmin) {
            throw new SecurityException("관리자만 멤버를 삭제할 수 있습니다.");
        }

        GroupMember member = repository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        repository.delete(member);
    }

    // DTO 내부 클래스
    public static class UserDTO {
        private Long userId;
        private String username;
        private String name;

        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getName() { return name; }
    }

    public static class LeaderDTO {
        private Long userId;

        public Long getUserId() { return userId; }
    }
}
