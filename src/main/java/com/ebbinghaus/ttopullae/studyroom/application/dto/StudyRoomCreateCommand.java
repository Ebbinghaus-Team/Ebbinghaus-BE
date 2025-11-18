package com.ebbinghaus.ttopullae.studyroom.application.dto;

import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;

public record StudyRoomCreateCommand(
        Long userId,
        String name,
        String description,
        String category,
        RoomType roomType
) {
}
