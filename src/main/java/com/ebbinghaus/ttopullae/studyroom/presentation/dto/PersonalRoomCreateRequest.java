package com.ebbinghaus.ttopullae.studyroom.presentation.dto;

import com.ebbinghaus.ttopullae.studyroom.application.dto.StudyRoomCreateCommand;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PersonalRoomCreateRequest(

        @NotBlank(message = "공부방 이름은 필수입니다")
        String name,

        String description,

        String category
) {
    public StudyRoomCreateCommand toCommand(Long userId) {
        return new StudyRoomCreateCommand(
                userId,
                name,
                description,
                category,
                RoomType.PERSONAL
        );
    }
}
