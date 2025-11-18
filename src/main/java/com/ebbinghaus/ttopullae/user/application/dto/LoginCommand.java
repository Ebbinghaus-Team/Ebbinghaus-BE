package com.ebbinghaus.ttopullae.user.application.dto;

/**
 * 로그인 요청을 서비스 계층으로 전달하는 Command 객체
 */
public record LoginCommand(
        String email,
        String password
) {
}