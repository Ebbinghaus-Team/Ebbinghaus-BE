package com.ebbinghaus.ttopullae.studyroom.presentation;

import com.ebbinghaus.ttopullae.studyroom.application.StudyRoomCreateCommand;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GroupRoomCreateRequest(
        // TODO: [인증 구현 후 제거] 임시로 Request Body에서 사용자 식별
        @NotNull(message = "사용자 ID는 필수입니다")
        Long userId,

        @NotBlank(message = "그룹 스터디 이름은 필수입니다")
        String name,

        String description,

        String category
) {
    public StudyRoomCreateCommand toCommand() {
        return new StudyRoomCreateCommand(
                userId,
                name,
                description,
                category,
                RoomType.GROUP
        );
    }
}