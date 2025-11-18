package com.ebbinghaus.ttopullae.studyroom.application.dto;

import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoomMember;
import java.time.LocalDateTime;

public record GroupRoomJoinResult(
    Long studyRoomId,
    String name,
    String category,
    String description,
    LocalDateTime joinedAt
) {

    public static GroupRoomJoinResult from(StudyRoomMember member) {
        return new GroupRoomJoinResult(
            member.getStudyRoom().getStudyRoomId(),
            member.getStudyRoom().getName(),
            member.getStudyRoom().getCategory(),
            member.getStudyRoom().getDescription(),
            member.getCreatedAt()
        );
    }
}