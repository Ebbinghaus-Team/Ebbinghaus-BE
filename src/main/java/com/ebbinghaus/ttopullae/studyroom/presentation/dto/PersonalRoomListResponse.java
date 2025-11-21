package com.ebbinghaus.ttopullae.studyroom.presentation.dto;

import com.ebbinghaus.ttopullae.studyroom.application.dto.PersonalRoomListResult;
import java.time.LocalDateTime;
import java.util.List;

public record PersonalRoomListResponse(
    List<PersonalRoomSummary> rooms,
    int totalCount
) {

    public record PersonalRoomSummary(
        Long studyRoomId,
        String name,
        String category,
        String description,
        int totalProblems,
        int graduatedProblems,
        LocalDateTime createdAt
    ) {
    }

    public static PersonalRoomListResponse from(PersonalRoomListResult result) {
        List<PersonalRoomSummary> summaries = result.rooms().stream()
                .map(info -> new PersonalRoomSummary(
                        info.studyRoomId(),
                        info.name(),
                        info.category(),
                        info.description(),
                        info.totalProblems(),
                        info.graduatedProblems(),
                        info.createdAt()
                ))
                .toList();

        return new PersonalRoomListResponse(summaries, summaries.size());
    }
}