package com.ebbinghaus.ttopullae.studyroom.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PersonalRoomListResult(
    List<PersonalRoomInfo> rooms
) {

    public record PersonalRoomInfo(
        Long studyRoomId,
        String name,
        String category,
        String description,
        int totalProblems,
        int graduatedProblems,
        LocalDateTime createdAt
    ) {
    }
}