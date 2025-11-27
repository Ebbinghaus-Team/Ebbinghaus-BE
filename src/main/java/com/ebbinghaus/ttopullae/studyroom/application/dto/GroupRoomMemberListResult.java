package com.ebbinghaus.ttopullae.studyroom.application.dto;

import java.util.List;

public record GroupRoomMemberListResult(
        Long studyRoomId,
        String studyRoomName,
        int totalMembers,
        List<MemberInfo> members
) {
    public record MemberInfo(
            Long userId,
            String username,
            boolean isOwner
    ) {}
}