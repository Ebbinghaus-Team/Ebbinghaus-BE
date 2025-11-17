package com.ebbinghaus.ttopullae.studyroom.presentation;

import com.ebbinghaus.ttopullae.studyroom.application.StudyRoomCreateCommand;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PersonalRoomCreateRequest(
        // TODO: [인증 구현 후 제거] 임시로 Request Body에서 사용자 식별
        @NotNull(message = "사용자 ID는 필수입니다")
        Long userId,

        @NotBlank(message = "공부방 이름은 필수입니다")
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
                RoomType.PERSONAL
        );
    }
}