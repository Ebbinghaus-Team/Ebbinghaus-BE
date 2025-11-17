package com.ebbinghaus.ttopullae.studyroom.exception;

/**
 * 스터디룸 관련 예외의 기본 클래스
 */
public class StudyRoomException extends RuntimeException {

    public StudyRoomException(String message) {
        super(message);
    }

    public StudyRoomException(String message, Throwable cause) {
        super(message, cause);
    }
}