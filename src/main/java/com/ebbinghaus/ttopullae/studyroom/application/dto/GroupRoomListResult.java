package com.ebbinghaus.ttopullae.studyroom.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GroupRoomListResult(
    List<GroupRoomInfo> rooms
) {

    public record GroupRoomInfo(
        Long studyRoomId,
        String name,
        String category,
        String description,
        String joinCode,
        int totalProblems,
        int graduatedProblems,
        int memberCount,
        LocalDateTime joinedAt
    ) {
    }
}