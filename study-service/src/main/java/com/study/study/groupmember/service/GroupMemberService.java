package com.study.study.groupmember.service;

import com.study.study.groupmember.domain.GroupMember;
import com.study.study.groupmember.dto.GroupMemberResponse;
import com.study.study.groupmember.repository.GroupMemberRepository;
import org.springframework.http.*;
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
    // 🔥 그룹 리더 여부 확인 (group-service = SAME PORT 10003)
    // ================================
    private boolean isLeader(Long groupId, Long requesterId) {

        // 같은 서비스이므로 10003
        String url = "http://study-service:10000/api/study-groups/" + groupId + "/leader";

        try {
            LeaderDTO leader = rt.getForObject(url, LeaderDTO.class);
            return leader != null && leader.getUserId().equals(requesterId);
        } catch (Exception e) {
            throw new IllegalArgumentException("그룹 리더 정보를 조회할 수 없습니다.");
        }
    }

    // ================================
    // 🔥 user-service API (Authorization 헤더 포함)
    // ================================
    private UserDTO getUser(Long userId, String authHeader) {
        String url = "http://user-service:10000/api/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null && !authHeader.isBlank()) {
            headers.set("Authorization", authHeader);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserDTO> response =
                rt.exchange(url, HttpMethod.GET, entity, UserDTO.class);

        return response.getBody();
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

        GroupMemberResponse res = GroupMemberResponse.fromEntity(member);

        // user-service 호출
        try {
            UserDTO user = getUser(member.getUserId(), authHeader);
            if (user != null) {
                res.setUsername(user.getUsername());
                res.setName(user.getName());
            }
        } catch (Exception e) {
            System.out.println("[WARN] user-service 호출 실패: " + e.getMessage());
        }

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