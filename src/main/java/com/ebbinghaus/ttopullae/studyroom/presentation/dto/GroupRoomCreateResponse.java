package com.ebbinghaus.ttopullae.studyroom.presentation.dto;

import com.ebbinghaus.ttopullae.studyroom.application.dto.StudyRoomCreateResult;

import java.time.LocalDateTime;

public record GroupRoomCreateResponse(
        Long studyRoomId,
        String name,
        String category,
        String description,
        String joinCode,
        LocalDateTime createdAt
) {
    public static GroupRoomCreateResponse from(StudyRoomCreateResult result) {
        return new GroupRoomCreateResponse(
                result.studyRoomId(),
                result.name(),
                result.category(),
                result.description(),
                result.joinCode(),
                result.createdAt()
        );
    }
}
