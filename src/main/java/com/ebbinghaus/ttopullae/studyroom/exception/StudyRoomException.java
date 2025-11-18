package com.ebbinghaus.ttopullae.studyroom.exception;

import com.ebbinghaus.ttopullae.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum StudyRoomException implements ExceptionCode {

    JOIN_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "참여 코드 생성 실패", "고유한 참여 코드 생성에 실패했습니다. 잠시 후 다시 시도해주세요."),
    STUDY_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "스터디룸을 찾을 수 없음", "요청한 참여 코드의 스터디룸이 존재하지 않습니다."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참여한 스터디룸", "이미 참여한 스터디룸입니다."),
    NOT_GROUP_ROOM(HttpStatus.BAD_REQUEST, "그룹 스터디가 아님", "참여 코드가 유효하지 않습니다. 개인 공부방에는 참여할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String title;
    private final String detail;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
