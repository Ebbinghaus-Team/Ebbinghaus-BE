package com.ebbinghaus.ttopullae.user.application.dto;

/**
 * 회원가입 요청을 서비스 계층으로 전달하는 Command 객체
 */
public record SignupCommand(
        String email,
        String password,
        String username,
        Boolean receiveNotifications
) {
}