package com.ebbinghaus.ttopullae.studyroom.presentation.dto;

import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomListResult;
import java.time.LocalDateTime;
import java.util.List;

public record GroupRoomListResponse(
    List<GroupRoomSummary> rooms,
    int totalCount
) {

    public record GroupRoomSummary(
        Long studyRoomId,
        String name,
        String category,
        String description,
        String joinCode,
        int totalProblems,
        int graduatedProblems,
        LocalDateTime joinedAt
    ) {
    }

    public static GroupRoomListResponse from(GroupRoomListResult result) {
        List<GroupRoomSummary> summaries = result.rooms().stream()
                .map(info -> new GroupRoomSummary(
                        info.studyRoomId(),
                        info.name(),
                        info.category(),
                        info.description(),
                        info.joinCode(),
                        info.totalProblems(),
                        info.graduatedProblems(),
                        info.joinedAt()
                ))
                .toList();

        return new GroupRoomListResponse(summaries, summaries.size());
    }
}
