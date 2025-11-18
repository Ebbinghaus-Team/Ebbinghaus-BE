package com.ebbinghaus.ttopullae.user.exception;

import com.ebbinghaus.ttopullae.global.exception.ExceptionCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum UserException implements ExceptionCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없음", "요청한 ID의 사용자가 존재하지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이메일 중복", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "잘못된 비밀번호", "비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰", "토큰이 유효하지 않습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "로그인 만료", "인증 정보가 만료되었습니다. 다시 로그인 해 주세요."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청", "로그인이 필요합니다.");

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
