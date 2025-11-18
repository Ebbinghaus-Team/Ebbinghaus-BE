package com.ebbinghaus.ttopullae.user.application.dto;

/**
 * 회원가입 결과를 서비스 계층에서 컨트롤러로 전달하는 Result 객체
 */
public record SignupResult(
        Long userId,
        String email,
        String username,
        Boolean receiveNotifications
) {
}