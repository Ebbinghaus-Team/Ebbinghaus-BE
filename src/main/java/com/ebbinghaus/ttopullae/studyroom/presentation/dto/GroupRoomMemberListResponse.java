package com.ebbinghaus.ttopullae.studyroom.presentation.dto;

import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomMemberListResult;
import java.util.List;

public record GroupRoomMemberListResponse(
        Long studyRoomId,
        String studyRoomName,
        int totalMembers,
        List<MemberInfo> members
) {
    public record MemberInfo(
            Long userId,
            String username,
            boolean isOwner
    ) {
        public static MemberInfo from(GroupRoomMemberListResult.MemberInfo memberInfo) {
            return new MemberInfo(
                    memberInfo.userId(),
                    memberInfo.username(),
                    memberInfo.isOwner()
            );
        }
    }

    public static GroupRoomMemberListResponse from(GroupRoomMemberListResult result) {
        List<MemberInfo> members = result.members().stream()
                .map(MemberInfo::from)
                .toList();

        return new GroupRoomMemberListResponse(
                result.studyRoomId(),
                result.studyRoomName(),
                result.totalMembers(),
                members
        );
    }
}