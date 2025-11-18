package com.ebbinghaus.ttopullae.studyroom.application.dto;

public record GroupRoomJoinCommand(
    Long userId,
    String joinCode
) {
}