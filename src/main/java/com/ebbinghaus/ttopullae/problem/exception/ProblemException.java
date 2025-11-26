package com.ebbinghaus.ttopullae.problem.exception;

import com.ebbinghaus.ttopullae.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ProblemException implements ExceptionCode {

    PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "문제를 찾을 수 없음", "요청한 ID의 문제가 존재하지 않습니다."),
    STUDYROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "스터디룸을 찾을 수 없음", "요청한 ID의 스터디룸이 존재하지 않습니다."),
    INVALID_MCQ_DATA(HttpStatus.BAD_REQUEST, "객관식 데이터 오류", "객관식 문제는 선택지 목록과 정답 인덱스가 필요합니다."),
    INVALID_OX_DATA(HttpStatus.BAD_REQUEST, "OX 데이터 오류", "OX 문제는 정답(true/false)이 필요합니다."),
    INVALID_SHORT_DATA(HttpStatus.BAD_REQUEST, "단답형 데이터 오류", "단답형 문제는 정답 텍스트가 필요합니다."),
    INVALID_SUBJECTIVE_DATA(HttpStatus.BAD_REQUEST, "서술형 데이터 오류", "서술형 문제는 모범 답안과 키워드 목록이 필요합니다."),
    INVALID_CHOICE_INDEX(HttpStatus.BAD_REQUEST, "잘못된 정답 인덱스", "정답 인덱스는 선택지 범위 내에 있어야 합니다."),
    ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "스터디룸 접근 권한 없음", "해당 스터디룸의 문제를 풀 수 있는 권한이 없습니다."),
    PROBLEM_NOT_ATTEMPTED(HttpStatus.BAD_REQUEST, "문제를 풀지 않음", "아직 풀지 않은 문제입니다. 문제를 먼저 풀어주세요."),
    REVIEW_INCLUSION_NOT_CONFIGURABLE(HttpStatus.BAD_REQUEST, "복습 루프 설정 변경 불가", "본인이 만든 문제는 복습 루프 포함 설정을 변경할 수 없습니다."),
    REVIEW_INCLUSION_ALREADY_CONFIGURED(HttpStatus.BAD_REQUEST, "복습 루프 설정 이미 완료", "복습 루프 포함 설정은 한 번만 변경할 수 있습니다.");

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
