package com.ebbinghaus.ttopullae.studyroom.presentation.dto;

import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomJoinCommand;
import jakarta.validation.constraints.NotBlank;

public record GroupRoomJoinRequest(
    @NotBlank(message = "참여 코드는 필수입니다.")
    String joinCode
) {

    public GroupRoomJoinCommand toCommand(Long userId) {
        return new GroupRoomJoinCommand(userId, joinCode);
    }
}