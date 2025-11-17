package com.ebbinghaus.ttopullae.studyroom.exception;

import com.ebbinghaus.ttopullae.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum StudyRoomException implements ExceptionCode {

    JOIN_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "참여 코드 생성 실패", "고유한 참여 코드 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");

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
