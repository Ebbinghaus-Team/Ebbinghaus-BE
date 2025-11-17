package com.ebbinghaus.ttopullae.studyroom.application.dto;

/**
 * 공부방 생성 서비스 명령 DTO
 * Controller에서 Service로 전달되는 데이터
 */
public record StudyRoomCreateCommand(
        Long userId,
        String name,
        String description,
        String category
) {
    /**
     * Request DTO를 Command로 변환
     */
    public static StudyRoomCreateCommand from(Long userId, String name, String description, String category) {
        return new StudyRoomCreateCommand(userId, name, description, category);
    }
}