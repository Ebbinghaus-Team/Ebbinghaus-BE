package com.ebbinghaus.ttopullae.user.presentation.dto;

import com.ebbinghaus.ttopullae.user.application.dto.LoginResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 로그인 API 응답 DTO
 */
@Schema(description = "로그인 응답")
public record LoginResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "사용자 이름", example = "홍길동")
        String username,

        @Schema(description = "JWT 액세스 토큰 (쿠키에도 설정됨)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken
) {
    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(
                result.userId(),
                result.email(),
                result.username(),
                result.accessToken()
        );
    }
}