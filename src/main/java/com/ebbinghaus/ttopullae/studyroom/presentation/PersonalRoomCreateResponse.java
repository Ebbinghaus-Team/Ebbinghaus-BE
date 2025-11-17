package com.ebbinghaus.ttopullae.studyroom.presentation;

import com.ebbinghaus.ttopullae.studyroom.application.StudyRoomCreateResult;

import java.time.LocalDateTime;

public record PersonalRoomCreateResponse(
        Long studyRoomId,
        String name,
        String category,
        String description,
        LocalDateTime createdAt
) {
    public static PersonalRoomCreateResponse from(StudyRoomCreateResult result) {
        return new PersonalRoomCreateResponse(
                result.studyRoomId(),
                result.name(),
                result.category(),
                result.description(),
                result.createdAt()
        );
    }
}