package com.ebbinghaus.ttopullae.user.presentation.dto;

import com.ebbinghaus.ttopullae.user.application.dto.SignupResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 회원가입 API 응답 DTO
 */
@Schema(description = "회원가입 응답")
public record SignupResponse(

        @Schema(description = "생성된 사용자 ID", example = "1")
        Long userId,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "사용자 이름", example = "홍길동")
        String username,

        @Schema(description = "알림 수신 여부", example = "true")
        Boolean receiveNotifications
) {
    public static SignupResponse from(SignupResult result) {
        return new SignupResponse(
                result.userId(),
                result.email(),
                result.username(),
                result.receiveNotifications()
        );
    }
}