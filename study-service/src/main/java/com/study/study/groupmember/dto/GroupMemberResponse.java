package com.study.study.groupmember.dto;

import com.study.study.groupmember.domain.GroupMember;
import java.time.LocalDateTime;

public class GroupMemberResponse {

    private Long memberId;
    private Long groupId;
    private Long userId;

    private String username; // nullable
    private String name;     // nullable

    private String role;
    private String status;
    private LocalDateTime joinedAt;

    // 🟡 기본 생성자 추가 (MSA DTO 권장 패턴)
    public GroupMemberResponse() {
    }

    public GroupMemberResponse(Long memberId,
                               Long groupId,
                               Long userId,
                               String username,
                               String name,
                               String role,
                               String status,
                               LocalDateTime joinedAt) {

        this.memberId = memberId;
        this.groupId = groupId;
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.role = role;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    // ===============================
    // Entity → Response 변환
    // ===============================
    public static GroupMemberResponse fromEntity(GroupMember member) {
        return new GroupMemberResponse(
                member.getMemberId(),
                member.getGroupId(),
                member.getUserId(),
                null,  // username 은 user-service 호출로 미래에 채움
                null,  // name 도 user-service 호출로 미래에 채움
                member.getRole().name(),
                member.getStatus().name(),
                member.getJoinedAt()
        );
    }

    // ===== Getter =====
    public Long getMemberId() { return memberId; }
    public Long getGroupId() { return groupId; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public LocalDateTime getJoinedAt() { return joinedAt; }

    // ===== Setter =====
    public void setUsername(String username) { this.username = username; }
    public void setName(String name) { this.name = name; }
}
