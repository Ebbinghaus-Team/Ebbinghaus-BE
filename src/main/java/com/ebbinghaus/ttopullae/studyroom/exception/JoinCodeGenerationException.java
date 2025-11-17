package com.ebbinghaus.ttopullae.studyroom.exception;

/**
 * 그룹 참여 코드 생성 실패 시 발생하는 예외
 * 일정 횟수 이상 재시도했음에도 고유한 코드 생성에 실패한 경우
 */
public class JoinCodeGenerationException extends StudyRoomException {

    public JoinCodeGenerationException() {
        super("고유한 참여 코드 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }

    public JoinCodeGenerationException(String message) {
        super(message);
    }
}