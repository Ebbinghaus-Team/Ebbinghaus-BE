package com.ebbinghaus.ttopullae.studyroom.application;

import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;

import java.time.LocalDateTime;

public record StudyRoomCreateResult(
        Long studyRoomId,
        String name,
        String category,
        String description,
        String joinCode,
        LocalDateTime createdAt
) {
    public static StudyRoomCreateResult from(StudyRoom studyRoom) {
        return new StudyRoomCreateResult(
                studyRoom.getStudyRoomId(),
                studyRoom.getName(),
                studyRoom.getCategory(),
                studyRoom.getDescription(),
                studyRoom.getJoinCode(),
                studyRoom.getCreatedAt()
        );
    }
}