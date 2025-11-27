package com.ebbinghaus.ttopullae.studyroom.application.dto;

/**
 * 그룹 공부방 문제 목록 조회 요청 Command
 *
 * @param userId 사용자 ID (JWT에서 추출)
 * @param studyRoomId 스터디룸 ID
 * @param filter 필터 타입 (ALL, NOT_IN_REVIEW, GATE_1, GATE_2, GRADUATED)
 */
public record GroupRoomProblemListCommand(
    Long userId,
    Long studyRoomId,
    String filter
) {
}