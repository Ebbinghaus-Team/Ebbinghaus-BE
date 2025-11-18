package com.ebbinghaus.ttopullae.user.application.dto;

/**
 * 로그인 결과를 서비스 계층에서 컨트롤러로 전달하는 Result 객체
 */
public record LoginResult(
        Long userId,
        String email,
        String username,
        String accessToken
) {
}