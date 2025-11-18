package com.ebbinghaus.ttopullae.studyroom.presentation.dto;

import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomJoinResult;
import java.time.LocalDateTime;

public record GroupRoomJoinResponse(
    Long studyRoomId,
    String name,
    String category,
    String description,
    LocalDateTime joinedAt
) {

    public static GroupRoomJoinResponse from(GroupRoomJoinResult result) {
        return new GroupRoomJoinResponse(
            result.studyRoomId(),
            result.name(),
            result.category(),
            result.description(),
            result.joinedAt()
        );
    }
}