package com.ebbinghaus.ttopullae.studyroom.application.dto;

/**
 * 개인 공부방 문제 목록 조회 요청 Command
 */
public record PersonalRoomProblemListCommand(
    Long userId,
    Long studyRoomId,
    String filter
) {
}